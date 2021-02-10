package net.buildtheearth.terraplusplus.control;

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import net.buildtheearth.terraplusplus.TerraMod;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.transform.OffsetProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.ProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.ScaleProjectionTransform;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * The world configuration GUI.
 * <p>
 * This is a hacky mess... but still many orders of magnitude better than the previous heap of refuse.
 *
 * @author DaPorkchop_
 */
public class EarthGui extends GuiScreen {
    protected static final int SRC_W = 2048;
    protected static final int SRC_H = 1024;
    protected static final Ref<int[]> SRC_CACHE = Ref.soft((IOSupplier<int[]>) () ->
            ImageIO.read(EarthGui.class.getResource("map.png")).getRGB(0, 0, SRC_W, SRC_H, null, 0, SRC_W));

    protected static final int SIZE = 1024;
    protected static final int VERTICAL_PADDING = 32;

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

    protected DynamicTexture texture;
    protected final GuiCreateWorld guiCreateWorld;

    protected EarthGeneratorSettings settings;

    protected GuiButton doneButton;
    protected GuiButton cancelButton;

    protected int imgSize;
    protected final List<Entry> entries = new ArrayList<>();
    protected final List<GuiTextField> textFields = new CopyOnWriteArrayList<>();

    public EarthGui(GuiCreateWorld guiCreateWorld, Minecraft mc) {
        super.buttonList = new CopyOnWriteArrayList<>();

        this.settings = EarthGeneratorSettings.parse(guiCreateWorld.chunkProviderSettingsJson);

        this.mc = mc;
        this.guiCreateWorld = guiCreateWorld;
    }

    private void updateMap() {
        if (this.texture == null) {
            this.texture = new DynamicTexture(SIZE, SIZE);
        }
        projectImage(this.settings.projection(), SRC_CACHE.get(), this.texture.getTextureData());
        this.texture.updateDynamicTexture();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);

        this.texture.deleteGlTexture();
        this.texture = null;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        this.imgSize = max(min(this.height - (VERTICAL_PADDING << 1), this.width >> 1), this.width - 400);

        this.buttonList.clear();
        this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done")));
        this.cancelButton = this.addButton(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));

        this.textFields.clear();

        RE_ASSEMBLE:
        if (!this.entries.isEmpty()) { //re-assemble projection
            EarthGeneratorSettings oldSettings = this.settings;

            try {
                for (Entry entry : this.entries) {
                    this.settings = entry.touchSettings(this.settings);
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.settings = oldSettings;
                break RE_ASSEMBLE;
            }
        }
        this.entries.clear();

        //add states
        int y = VERTICAL_PADDING;
        y += this.addEntry(new ProjectionEntry(this.settings, this, 5, y, this.width - this.imgSize - 10)).height();
        y += this.addEntry(new PaddingEntry(10)).height();
        y += this.addEntry(new ToggleEntry(this, 5, y, this.width - this.imgSize - 10, this.settings.useDefaultHeights(), "use_default_heights", EarthGeneratorSettings::withUseDefaultHeights)).height();
        y += this.addEntry(new ToggleEntry(this, 5, y, this.width - this.imgSize - 10, this.settings.useDefaultTreeCover(), "use_default_trees", EarthGeneratorSettings::withUseDefaultTreeCover)).height();
        y += this.addEntry(new PaddingEntry(10)).height();
        y += this.addEntry(new CWGEntry(this.settings, this, 5, y, this.width - this.imgSize - 10)).height();

        this.updateMap();
    }

    protected Entry addEntry(Entry entry) {
        this.entries.add(entry);
        return entry;
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
        this.drawCenteredString(this.fontRenderer, I18n.format(TerraMod.MODID + ".gui.header"), this.width >> 1, VERTICAL_PADDING >> 1, 0xFFFFFFFF);

        //render map texture
        GlStateManager.bindTexture(this.texture.getGlTextureId());
        drawScaledCustomSizeModalRect(this.width - this.imgSize, VERTICAL_PADDING, 0, 0, SIZE, SIZE, this.imgSize, this.imgSize, SIZE, SIZE);

        //render list
        int y = VERTICAL_PADDING;
        for (Entry entry : this.entries) {
            entry.render(this, 5, y, this.width - this.imgSize - 10);
            y += entry.height();
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
            textField.textboxKeyTyped(typedChar, keyCode);
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
            boolean updateQueued = false;
            for (GuiTextField textField : this.textFields) {
                boolean focused = textField.isFocused();
                textField.mouseClicked(mouseX, mouseY, mouseEvent);
                if (!textField.isFocused() && focused) {
                    updateQueued = true;
                }
            }
            for (GuiButton button : this.buttonList) {
                if (button.mousePressed(this.mc, mouseX, mouseY)) {
                    updateQueued = true;
                }
            }
            if (updateQueued) {
                this.initGui();
            }
        }
    }

    protected interface Entry {
        int height();

        default void render(EarthGui gui, int x, int y, int width) {
        }

        default EarthGeneratorSettings touchSettings(EarthGeneratorSettings settings) {
            return settings;
        }
    }

    protected static class ProjectionEntry implements Entry {
        protected final List<SubEntry> entries = new ArrayList<>();
        @Getter
        protected int height = 30;

        public ProjectionEntry(EarthGeneratorSettings settings, EarthGui gui, int x, int y, int width) {
            gui.addButton(new GuiButton(0, x + (width >> 1), y, width >> 1, 20, I18n.format(TerraMod.MODID + ".gui.transformation.add")) {
                @Override
                public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                    if (super.mousePressed(mc, mouseX, mouseY)) {
                        ProjectionEntry.this.entries.add(0, Transformation.values()[0].newSubEntry(ProjectionEntry.this, gui, 0, 0, 1));
                        return true;
                    }
                    return false;
                }
            });

            GeographicProjection projection = settings.projection();
            while (projection instanceof ProjectionTransform) {
                ProjectionTransform transform = (ProjectionTransform) projection;
                TransformEntry entry = Transformation.valueOf(GlobalParseRegistries.PROJECTIONS.inverse().get(transform.getClass()))
                        .newSubEntry(this, gui, x + 10, y + this.height, width - 10);
                entry.initFrom(transform);
                this.height += entry.height() + 5;
                this.entries.add(entry);

                projection = transform.delegate();
            }

            SubEntry entry = new RootEntry(projection, gui, x + 10, y + this.height, width - 10);
            this.height += entry.height();
            this.entries.add(entry);
        }

        @Override
        public void render(EarthGui gui, int x, int y, int width) {
            gui.fontRenderer.drawString(I18n.format(TerraMod.MODID + ".gui.projection"), x, y + (20 - 8) / 2, 0xFFFFFFFF, true);

            y += 30;

            for (SubEntry entry : this.entries) {
                entry.render(gui, x + 10, y, width - 10);
                y += entry.height() + 5;
            }
        }

        @Override
        public EarthGeneratorSettings touchSettings(EarthGeneratorSettings settings) {
            StringBuilder builder = new StringBuilder();
            for (int i = this.entries.size() - 1; i >= 0; i--) {
                this.entries.get(i).toJson(builder);
            }
            return settings.withProjection(GeographicProjection.parse(builder.toString()));
        }

        protected enum Transformation {
            flip_horizontal,
            flip_vertical,
            swap_axes,
            offset {
                @Override
                protected TransformEntry newSubEntry(ProjectionEntry entry, EarthGui gui, int x, int y, int width) {
                    return new ParameterizedTransformEntry(this, entry, gui, x, y, width, TerraMod.MODID + ".gui.transformation.offset", "dx", "dy") {
                        @Override
                        public void initFrom(ProjectionTransform in) {
                            if (in instanceof OffsetProjectionTransform) {
                                OffsetProjectionTransform transform = (OffsetProjectionTransform) in;
                                this.textFields[0].setText(String.valueOf(transform.dx()));
                                this.textFields[1].setText(String.valueOf(transform.dy()));
                            } else {
                                this.textFields[0].setText("1.0");
                                this.textFields[1].setText("1.0");
                            }
                        }

                        @Override
                        protected void appendValue(StringBuilder out, int i) {
                            out.append(Double.parseDouble(this.textFields[i].getText()));
                        }
                    };
                }
            },
            scale {
                @Override
                protected TransformEntry newSubEntry(ProjectionEntry entry, EarthGui gui, int x, int y, int width) {
                    return new ParameterizedTransformEntry(this, entry, gui, x, y, width, TerraMod.MODID + ".gui.transformation.scale", "x", "y") {
                        @Override
                        public void initFrom(ProjectionTransform in) {
                            if (in instanceof ScaleProjectionTransform) {
                                ScaleProjectionTransform transform = (ScaleProjectionTransform) in;
                                this.textFields[0].setText(String.valueOf(transform.x()));
                                this.textFields[1].setText(String.valueOf(transform.y()));
                            } else {
                                this.textFields[0].setText("1.0");
                                this.textFields[1].setText("1.0");
                            }
                        }

                        @Override
                        protected void appendValue(StringBuilder out, int i) {
                            out.append(Double.parseDouble(this.textFields[i].getText()));
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

            protected TransformEntry newSubEntry(ProjectionEntry entry, EarthGui gui, int x, int y, int width) {
                return new TransformEntry(this, entry, gui, x, y, width);
            }
        }

        protected interface SubEntry {
            int height();

            default void render(EarthGui gui, int x, int y, int width) {
            }

            void toJson(StringBuilder out);
        }

        protected static class TransformEntry implements SubEntry {
            protected final Transformation transformation;

            public TransformEntry(Transformation transformation, ProjectionEntry entry, EarthGui gui, int x, int y, int width) {
                this.transformation = transformation;

                gui.addButton(new GuiButton(0, x, y, 20, 20, "\u25B2") { //up
                    @Override
                    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                        if (super.mousePressed(mc, mouseX, mouseY)) {
                            entry.entries.remove(TransformEntry.this);
                            entry.entries.add(0, TransformEntry.this);
                            return true;
                        }
                        return false;
                    }
                });
                gui.addButton(new GuiButton(0, x + 20, y, 20, 20, "\u25BC") { //down
                    @Override
                    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                        if (super.mousePressed(mc, mouseX, mouseY)) {
                            entry.entries.remove(TransformEntry.this);
                            entry.entries.add(entry.entries.size() - 1, TransformEntry.this);
                            return true;
                        }
                        return false;
                    }
                });
                gui.addButton(new GuiButton(0, x + 40, y, 20, 20, "\u2716") { //remove
                    @Override
                    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                        if (super.mousePressed(mc, mouseX, mouseY)) {
                            entry.entries.remove(TransformEntry.this);
                            return true;
                        }
                        return false;
                    }
                });

                gui.addButton(new GuiButton(0, x + 60, y, width - 60, 20, I18n.format(TerraMod.MODID + ".gui.transformation." + transformation.name())) {
                    @Override
                    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                        if (super.mousePressed(mc, mouseX, mouseY)) {
                            entry.entries.set(entry.entries.indexOf(TransformEntry.this), transformation.next.newSubEntry(entry, gui, 0, 0, 1));
                            return true;
                        }
                        return false;
                    }
                });
            }

            public void initFrom(ProjectionTransform transform) {
                //no-op
            }

            @Override
            public int height() {
                return 20;
            }

            @Override
            public void toJson(StringBuilder out) {
                out.insert(0, "{\"" + this.transformation.name() + "\":{\"delegate\":");
                this.appendOptions(out);
                out.append("}}");
            }

            protected void appendOptions(StringBuilder out) {
                //no-op
            }
        }

        protected static abstract class ParameterizedTransformEntry extends TransformEntry {
            protected final String fieldName;
            protected final String[] paramNames;
            protected final GuiTextField[] textFields;

            public ParameterizedTransformEntry(Transformation transformation, ProjectionEntry entry, EarthGui gui, int x, int y, int width, String fieldName, String... paramNames) {
                super(transformation, entry, gui, x, y, width);

                this.fieldName = fieldName;
                this.paramNames = paramNames;

                int maxLen = Arrays.stream(paramNames)
                                     .map(s -> fieldName + '.' + s)
                                     .map(I18n::format)
                                     .mapToInt(gui.fontRenderer::getStringWidth)
                                     .max().getAsInt() + 5;

                this.textFields = new GuiTextField[paramNames.length];
                for (int i = 0; i < paramNames.length; i++) {
                    this.textFields[i] = gui.addTextField(x + maxLen, y + super.height() + 2 + i * 24, width - maxLen - 2, 20);
                }

                this.initFrom(null);
            }

            @Override
            public int height() {
                return super.height() + 2 + this.textFields.length * 24;
            }

            @Override
            public void render(EarthGui gui, int x, int y, int width) {
                for (int i = 0; i < this.paramNames.length; i++) {
                    gui.fontRenderer.drawString(I18n.format(this.fieldName + '.' + this.paramNames[i]), x, y + super.height() + 2 + i * 24 + (20 - 8) / 2, -1, true);
                }
            }

            @Override
            protected void appendOptions(StringBuilder out) {
                for (int i = 0; i < this.paramNames.length; i++) {
                    out.append(",\"").append(this.paramNames[i]).append("\":");
                    this.appendValue(out, i);
                }
            }

            protected abstract void appendValue(StringBuilder out, int i);
        }

        protected static class RootEntry implements SubEntry {
            protected static final String[] PROJECTION_NAMES = GlobalParseRegistries.PROJECTIONS.entrySet().stream()
                    .filter(e -> !ProjectionTransform.class.isAssignableFrom(e.getValue()))
                    .map(Map.Entry::getKey)
                    .toArray(String[]::new);

            protected final int initialIndex;
            protected int index;

            protected final String fieldName;
            protected final Map<String, Object> properties;
            protected final GuiTextField[] textFields;

            public RootEntry(GeographicProjection projection, EarthGui gui, int x, int y, int width) {
                String projectionName = GlobalParseRegistries.PROJECTIONS.inverse().get(projection.getClass());
                this.initialIndex = this.index = PArrays.indexOf(PROJECTION_NAMES, projectionName);

                gui.addButton(new GuiButton(0, x, y, width, 20, I18n.format(this.fieldName = TerraMod.MODID + ".gui.projection." + projectionName)) {
                    @Override
                    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                        if (super.mousePressed(mc, mouseX, mouseY)) {
                            RootEntry.this.index = (RootEntry.this.index + 1) % PROJECTION_NAMES.length;
                            return true;
                        }
                        return false;
                    }
                });

                this.properties = projection.properties();

                int maxLen = this.properties.keySet().stream()
                                     .map(s -> this.fieldName + '.' + s)
                                     .map(I18n::format)
                                     .mapToInt(gui.fontRenderer::getStringWidth)
                                     .max().orElse(0) + 5;

                this.textFields = new GuiTextField[this.properties.size()];
                int i = 0;
                for (Map.Entry<String, Object> entry : this.properties.entrySet()) {
                    this.textFields[i] = gui.addTextField(x + maxLen, y + 20 + 2 + i * 24, width - maxLen - 2, 20);
                    this.textFields[i].setText(Objects.toString(entry.getValue()));
                    i++;
                }
            }

            @Override
            public int height() {
                return 20 + 2 + this.textFields.length * 24;
            }

            @Override
            public void render(EarthGui gui, int x, int y, int width) {
                int i = 0;
                for (String s : this.properties.keySet()) {
                    gui.fontRenderer.drawString(I18n.format(this.fieldName + '.' + s), x, y + 20 + 2 + i * 24 + (20 - 8) / 2, -1, true);
                    i++;
                }
            }

            @Override
            public void toJson(StringBuilder out) {
                checkArg(out.length() == 0, "must be first element in json output!");
                out.append("{\"").append(PROJECTION_NAMES[this.index]).append("\":{");
                if (this.initialIndex == this.index) {
                    int i = 0;
                    for (Map.Entry<String, Object> entry : this.properties.entrySet()) {
                        if (i != 0) {
                            out.append(',');
                        }
                        out.append('"').append(entry.getKey()).append("\":");
                        boolean num = entry.getValue() instanceof Number;
                        if (!num) {
                            out.append('"');
                        }
                        out.append(this.textFields[i].getText());
                        if (!num) {
                            out.append('"');
                        }
                        i++;
                    }
                }
                out.append("}}");
            }
        }
    }

    protected static class ToggleEntry implements Entry {
        protected final BiFunction<EarthGeneratorSettings, Boolean, EarthGeneratorSettings> touch;
        protected boolean value;

        public ToggleEntry(EarthGui gui, int x, int y, int width, boolean value, String name, BiFunction<EarthGeneratorSettings, Boolean, EarthGeneratorSettings> touch) {
            this.touch = touch;
            this.value = value;

            gui.addButton(new GuiButton(0, x, y, width, 20, I18n.format(TerraMod.MODID + ".gui." + name) + ": " + I18n.format("options." + (value ? "on" : "off"))) {
                @Override
                public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                    if (super.mousePressed(mc, mouseX, mouseY)) {
                        ToggleEntry.this.value = !ToggleEntry.this.value;
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

        @Override
        public EarthGeneratorSettings touchSettings(EarthGeneratorSettings settings) {
            return this.touch.apply(settings, this.value);
        }
    }

    @RequiredArgsConstructor
    @Getter
    protected static class PaddingEntry implements Entry {
        protected final int height;
    }

    protected static class CWGEntry implements Entry {
        protected String text;

        public CWGEntry(EarthGeneratorSettings settings, EarthGui gui, int x, int y, int width) {
            int text = gui.fontRenderer.getStringWidth(I18n.format(TerraMod.MODID + ".gui.cwg")) + 5;
            x += text;
            width -= text;

            this.text = settings.cwg();

            gui.addButton(new GuiButton(0, x + width - 20, y, 20, 20, "...") {
                @Override
                public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                    if (super.mousePressed(mc, mouseX, mouseY)) {
                        GuiCreateWorld fakeParent = new GuiCreateWorld(null);
                        fakeParent.chunkProviderSettingsJson = CWGEntry.this.text;

                        MinecraftForge.EVENT_BUS.register(new Object() {
                            @SubscribeEvent
                            public void onGuiOpen(GuiOpenEvent event) {
                                if (event.getGui() == fakeParent) {
                                    CWGEntry.this.text = fakeParent.chunkProviderSettingsJson;
                                    event.setGui(gui);
                                    MinecraftForge.EVENT_BUS.unregister(this);
                                }
                            }
                        });

                        for (WorldType type : WorldType.WORLD_TYPES) { //find CustomCubicWorldType instance
                            if (type instanceof CustomCubicWorldType) {
                                type.onCustomizeButton(mc, fakeParent);
                                break;
                            }
                        }
                    }
                    return false;
                }
            });
        }

        @Override
        public int height() {
            return 20;
        }

        @Override
        public void render(EarthGui gui, int x, int y, int width) {
            gui.fontRenderer.drawString(I18n.format(TerraMod.MODID + ".gui.cwg"), x, y + (20 - 8) / 2, 0xFFFFFFFF, true);
        }

        @Override
        public EarthGeneratorSettings touchSettings(EarthGeneratorSettings settings) {
            return settings.withCwg(this.text);
        }
    }
}
