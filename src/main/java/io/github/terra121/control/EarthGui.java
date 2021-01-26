package io.github.terra121.control;

import io.github.terra121.TerraMod;
import io.github.terra121.config.GlobalParseRegistries;
import io.github.terra121.generator.EarthGeneratorSettings;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.projection.transform.OffsetProjectionTransform;
import io.github.terra121.projection.transform.ProjectionTransform;
import io.github.terra121.projection.transform.ScaleProjectionTransform;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.util.PArrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.Math.*;

public class EarthGui extends GuiScreen {
    protected static final int SRC_W = 2048;
    protected static final int SRC_H = 1024;
    protected static final Ref<int[]> SRC_CACHE = Ref.soft((IOSupplier<int[]>) () ->
            ImageIO.read(EarthGui.class.getResource("map.png")).getRGB(0, 0, SRC_W, SRC_H, null, 0, SRC_W));

    protected static final int SIZE = 1024;
    protected static final int VERTICAL_PADDING = 32;

    protected static final String[] PROJECTION_NAMES = GlobalParseRegistries.PROJECTIONS.entrySet().stream()
            .filter(e -> !ProjectionTransform.class.isAssignableFrom(e.getValue()))
            .map(Map.Entry::getKey)
            .toArray(String[]::new);

    protected static void projectImage(@NonNull GeographicProjection projection, @NonNull int[] src, @NonNull int[] dst) {
        //scale should be able to fit whole earth inside texture
        double[] bounds = projection.bounds();

        double minX = min(bounds[0], bounds[2]);
        double maxX = max(bounds[0], bounds[2]);
        double minY = min(bounds[1], bounds[3]);
        double maxY = max(bounds[1], bounds[3]);
        double dx = maxX - minX;
        double dy = maxY - minY;
        double scale = max(dx, dy) / (double) SIZE;

        Arrays.fill(dst, 0); //fill with transparent pixels

        //acually set map data (in parallel, because some projections are slow)
        IntStream.range(0, SIZE).parallel()
                .forEach(yi -> {
                    double y = yi * scale + minY;
                    if (y <= minY || y >= maxY) { //skip entire row if y value out of projection bounds
                        return;
                    }

                    for (int xi = 0; xi < SIZE; xi++) {
                        double x = xi * scale + minX;
                        if (x <= minX || x >= maxX) { //sample out of bounds, skip it
                            continue;
                        }

                        try {
                            double[] projected = projection.toGeo(x, y);
                            int lon = (int) (((projected[0] + 180.0d) * (SRC_W / 360.0d)));
                            int lat = (int) (((projected[1] + 90.0d) * (SRC_H / 180.0d)));
                            if (lon < 0 || lon >= SRC_W || lat < 0 || lat >= SRC_H) { //projected sample is out of bounds
                                continue;
                            }

                            dst[yi * SIZE + xi] = src[lat * SRC_W + lon];
                        } catch (OutOfProjectionBoundsException ignored) {
                            //sample out of bounds, skip it
                        }
                    }
                });
    }

    protected final DynamicTexture texture = new DynamicTexture(SIZE, SIZE);
    protected final ResourceLocation textureLocation;
    protected final GuiCreateWorld guiCreateWorld;

    protected EarthGeneratorSettings settings;

    protected GuiButton doneButton;
    protected GuiButton cancelButton;

    protected int imgSize;
    protected final List<State> states = new ArrayList<>();
    protected final List<GuiTextField> textFields = new ArrayList<>();

    public EarthGui(GuiCreateWorld guiCreateWorld, Minecraft mc) {
        this.settings = EarthGeneratorSettings.parse(guiCreateWorld.chunkProviderSettingsJson);
        this.textureLocation = mc.renderEngine.getDynamicTextureLocation("terraplusplus_map", this.texture);

        this.mc = mc;
        this.guiCreateWorld = guiCreateWorld;

        this.updateMap();
    }

    private void updateMap() {
        projectImage(this.settings.projection(), SRC_CACHE.get(), this.texture.getTextureData());
        this.texture.updateDynamicTexture();
    }

    @Override
    public void initGui() {
        this.imgSize = min(this.height - (VERTICAL_PADDING << 1), this.width >> 1);

        this.buttonList.clear();
        this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done")));
        this.cancelButton = this.addButton(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));

        this.textFields.clear();

        if (!this.states.isEmpty()) { //re-assemble projection
            StringBuilder builder = new StringBuilder();
            for (int i = this.states.size() - 1; i >= 0; i--) {
                State state = this.states.get(i);
                if (state instanceof ProjectionState) {
                    builder.append("{\"").append(PROJECTION_NAMES[((ProjectionState) state).index]).append("\":{}}");
                } else if (state instanceof TransformState) {
                    ((TransformState) state).toProjectionJson(builder);
                }
            }
            this.states.clear();

            this.settings = this.settings.withProjection(GeographicProjection.parse(builder.toString()));
            this.updateMap();
        }

        //add states
        GeographicProjection projection = this.settings.projection();
        while (projection instanceof ProjectionTransform) {
            TransformState state = Transformation.valueOf(GlobalParseRegistries.PROJECTIONS.inverse().get(projection.getClass())).newState();
            this.states.add(state);
            projection = ((ProjectionTransform) projection).delegate();
        }
        this.states.add(new ProjectionState(PArrays.indexOf(PROJECTION_NAMES, GlobalParseRegistries.PROJECTIONS.inverse().get(projection.getClass()))));

        int y = VERTICAL_PADDING;
        for (State state : this.states) {
            state.init(this, 5, y, this.width - this.imgSize - 10);
            y += state.height() + 5;
        }

        int i = 0;
        projection = this.settings.projection();
        while (projection instanceof ProjectionTransform) {
            ((TransformState) this.states.get(i++)).initFrom((ProjectionTransform) projection);
            projection = ((ProjectionTransform) projection).delegate();
        }
    }

    @Override
    protected <T extends GuiButton> T addButton(@NonNull T button) {
        button.id = this.buttonList.size();
        return super.addButton(button);
    }

    protected GuiTextField addTextField(int x, int y, int width, int height) {
        GuiTextField textField = new GuiTextField(this.textFields.size(), this.fontRenderer, x, y, width, height);
        this.textFields.add(textField);
        return textField;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format(TerraMod.MODID + ".worldSettings.title"), this.width >> 1, VERTICAL_PADDING >> 1, 0xFFFFFFFF);

        //render map texture
        this.mc.renderEngine.bindTexture(this.textureLocation);
        drawScaledCustomSizeModalRect(this.width - this.imgSize, VERTICAL_PADDING, 0, 0, SIZE, SIZE, this.imgSize, this.imgSize, SIZE, SIZE);

        //render list
        int y = VERTICAL_PADDING;
        for (State state : this.states) {
            state.render(this, 5, y, this.width - this.imgSize - 10);
            y += state.height() + 5;
        }

        //render text fields
        for (GuiTextField textField : this.textFields) {
            textField.drawTextBox();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        for (GuiTextField textField : this.textFields) {
            textField.updateCursorCounter();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        for (GuiTextField textField : this.textFields) {
            if (textField.textboxKeyTyped(typedChar, keyCode)) {
                return;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseEvent) {
        if (this.doneButton.mousePressed(this.mc, mouseX, mouseY)) {
            this.guiCreateWorld.chunkProviderSettingsJson = this.settings.toString(); //save settings
            this.mc.displayGuiScreen(this.guiCreateWorld); //exit
        } else if (this.cancelButton.mousePressed(this.mc, mouseX, mouseY)) {
            this.mc.displayGuiScreen(this.guiCreateWorld); //exit without saving
        } else {
            for (GuiTextField textField : this.textFields) {
                if (textField.mouseClicked(mouseX, mouseY, mouseEvent)) {
                    return;
                }
            }
            for (GuiButton button : this.buttonList) {
                if (button.mousePressed(this.mc, mouseX, mouseY)) {
                    return;
                }
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        this.mc.renderEngine.deleteTexture(this.textureLocation);
    }

    @Getter
    protected enum Transformation {
        flip_vertical,
        swap_axes,
        offset {
            @Override
            protected TransformState newState() {
                return new TransformState(this) {
                    GuiTextField x;
                    GuiTextField y;

                    @Override
                    public void init(EarthGui gui, int x, int y, int width) {
                        super.init(gui, x, y, width);

                        this.x = gui.addTextField(x + 20, y + super.height(), width - 20, 20);
                        this.x.setText("0.0");
                        this.y = gui.addTextField(x + 20, y + super.height() + 20, width - 20, 20);
                        this.y.setText("0.0");
                    }

                    @Override
                    public int height() {
                        return super.height() + 40;
                    }

                    @Override
                    public void toProjectionJson(StringBuilder builder) {
                        builder.insert(0, "{\"" + this.transformation.name() + "\":{\"delegate\":");
                        builder.append("},\"dx\":").append(Double.parseDouble(this.x.getText()))
                                .append(",\"dy\":").append(Double.parseDouble(this.y.getText()))
                                .append('}');
                    }

                    @Override
                    public void initFrom(ProjectionTransform transform) {
                        if (transform instanceof OffsetProjectionTransform) {
                            this.x.setText(String.valueOf(((OffsetProjectionTransform) transform).dx()));
                            this.y.setText(String.valueOf(((OffsetProjectionTransform) transform).dy()));
                        }
                    }
                };
            }
        },
        scale {
            @Override
            protected TransformState newState() {
                return new TransformState(this) {
                    GuiTextField x;
                    GuiTextField y;

                    @Override
                    public void init(EarthGui gui, int x, int y, int width) {
                        super.init(gui, x, y, width);

                        this.x = gui.addTextField(x + 20, y + super.height(), width - 20, 20);
                        this.x.setText("0.0");
                        this.y = gui.addTextField(x + 20, y + super.height() + 20, width - 20, 20);
                        this.y.setText("0.0");
                    }

                    @Override
                    public int height() {
                        return super.height() + 40;
                    }

                    @Override
                    public void toProjectionJson(StringBuilder builder) {
                        builder.insert(0, "{\"" + this.transformation.name() + "\":{\"delegate\":");
                        builder.append("},\"x\":").append(Double.parseDouble(this.x.getText()))
                                .append(",\"y\":").append(Double.parseDouble(this.y.getText()))
                                .append('}');
                    }

                    @Override
                    public void initFrom(ProjectionTransform transform) {
                        if (transform instanceof ScaleProjectionTransform) {
                            this.x.setText(String.valueOf(((ScaleProjectionTransform) transform).x()));
                            this.y.setText(String.valueOf(((ScaleProjectionTransform) transform).y()));
                        }
                    }
                };
            }
        };

        static {
            Transformation[] values = values();
            for (int i = 0; i < values.length; i++) {
                values[i].next = values[(i + 1) % values.length];
            }
        }

        private Transformation next;

        protected TransformState newState() {
            return new TransformState(this);
        }
    }

    protected interface State {
        void init(EarthGui gui, int x, int y, int width);

        int height();

        default void render(EarthGui gui, int x, int y, int width) {
        }
    }

    @RequiredArgsConstructor
    protected static class TransformState implements State {
        @NonNull
        protected EarthGui.Transformation transformation;

        @Override
        public void init(EarthGui gui, int x, int y, int width) {
            gui.addButton(new GuiButton(0, x, y, width, 20, I18n.format("terra121.transformation." + this.transformation)) {
                @Override
                public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                    if (super.mousePressed(mc, mouseX, mouseY)) {
                        gui.states.set(gui.states.indexOf(TransformState.this), TransformState.this.transformation.next.newState());
                        gui.initGui();
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public int height() {
            return 20;
        }

        public void initFrom(ProjectionTransform transform) {
            //no-op
        }

        public void toProjectionJson(StringBuilder builder) {
            builder.insert(0, "{\"" + this.transformation.name() + "\":{\"delegate\":");
            builder.append("}}");
        }
    }

    @AllArgsConstructor
    protected static class ProjectionState implements State {
        protected int index;

        @Override
        public void init(EarthGui gui, int x, int y, int width) {
            gui.addButton(new GuiButton(0, x, y, width, 20, I18n.format("terra121.projection." + PROJECTION_NAMES[this.index])) {
                @Override
                public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                    if (super.mousePressed(mc, mouseX, mouseY)) {
                        ProjectionState.this.index = (ProjectionState.this.index + 1) % PROJECTION_NAMES.length;
                        gui.initGui();
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public int height() {
            return 20;
        }
    }
}
