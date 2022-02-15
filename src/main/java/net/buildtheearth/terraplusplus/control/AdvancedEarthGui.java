package net.buildtheearth.terraplusplus.control;

import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.TerraMod;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.transform.ClampProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.OffsetProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.ProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.RotateProjectionTransform;
import net.buildtheearth.terraplusplus.projection.transform.ScaleProjectionTransform;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;
import net.daporkchop.lib.common.util.PArrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * The advanced world configuration GUI.
 * <p>
 * This is a hacky mess... but still many orders of magnitude better than the previous heap of refuse. - DaPorkchop_
 * <p>
 * Sorry, but it is even more a mess now. I probably should have re-written this from scratch instead of hacking over some already hacky code - SmylerMC
 * <p>
 * I added even more hacks! As much as I hate to admit it, this class will probably never be cleaned up. Oh well. - DaPorkchop_
 *
 * @author DaPorkchop_
 * @author SmylerMC
 */
public class AdvancedEarthGui extends GuiScreen {
    protected static final int SRC_W = 2048;
    protected static final int SRC_H = 1024;
    protected static final Cached<int[]> SRC_CACHE = Cached.global((IOSupplier<int[]>) () ->
            ImageIO.read(AdvancedEarthGui.class.getResource("map.png")).getRGB(0, 0, SRC_W, SRC_H, null, 0, SRC_W),
            ReferenceStrength.SOFT);

    protected static final int VERTICAL_PADDING = 32;

    public static final ResourceLocation DIRECTIONS_TEXTURE = new ResourceLocation(TerraConstants.MODID, "textures/directions.png");

    protected final GuiScreen parent;
    protected Consumer<String> whenDone;

    protected EarthGeneratorSettings settings;
    protected long worldSizeX, worldSizeY;

    protected GuiButton doneButton;
    protected GuiButton cancelButton;

    protected int imgSize;
    protected final List<Entry> entries = new ArrayList<>();
    protected int entriesWidth;
    protected int entriesHeight;
    protected final List<GuiButton> nonTranslatedButtons = new CopyOnWriteArrayList<>();
    protected final List<GuiTextField> textFields = new CopyOnWriteArrayList<>();
    @Deprecated
    protected ProjectionEntry projectionEntry;

    protected ProjectionPreview preview;

    private int lastClickMoveX, lastClickMoveY;

    protected int deltaY;
    protected boolean isDraggingScrollbar;

    public AdvancedEarthGui(GuiScreen parent, EarthGeneratorSettings settings, Consumer<String> whenDone) {
        super.buttonList = new CopyOnWriteArrayList<>();

        this.settings = settings;
        this.whenDone = whenDone;

        this.mc = Minecraft.getMinecraft();
        this.parent = parent;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);

        this.preview.finish();
        this.preview = null;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        if (this.preview == null) {
            this.preview = new ProjectionPreview();
        }

        this.imgSize = max(min(this.height - (VERTICAL_PADDING << 1), this.width >> 1), this.width - 400);

        this.buttonList.clear();
        this.nonTranslatedButtons.clear();
        this.nonTranslatedButtons.add(this.doneButton = new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done")));
        this.nonTranslatedButtons.add(this.cancelButton = new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));

        this.textFields.clear();

        RE_ASSEMBLE:
        if (!this.entries.isEmpty()) { //re-assemble projection
            EarthGeneratorSettings oldSettings = this.settings;

            try {
                for (Entry entry : this.entries) {
                    this.settings = entry.touchSettings(this.settings);
                }
            } catch (Exception e) {
                TerraMod.LOGGER.catching(e);
                this.settings = oldSettings;
                break RE_ASSEMBLE;
            }
        }
        this.entries.clear();

        //add states
        this.entriesWidth = this.width - this.imgSize - 10 - 6;
        int y = VERTICAL_PADDING;
        y += this.addEntry(new ProjectionEntry(this.settings, this, 5, y, this.entriesWidth)).height();
        y += this.addEntry(new PaddingEntry(10)).height();
        y += this.addEntry(new ToggleEntry(this, 5, y, this.entriesWidth, this.settings.useDefaultHeights(), "use_default_heights", EarthGeneratorSettings::withUseDefaultHeights)).height();
        y += this.addEntry(new ToggleEntry(this, 5, y, this.entriesWidth, this.settings.useDefaultTreeCover(), "use_default_trees", EarthGeneratorSettings::withUseDefaultTreeCover)).height();
        y += this.addEntry(new PaddingEntry(10)).height();
        y += this.addEntry(new CWGEntry(this.settings, this, 5, y, this.entriesWidth)).height();
        y += this.addEntry(new PaddingEntry(10)).height();
        y += this.addEntry(new EnumSelectionListEntry<>(this, 5, y, this.entriesWidth, this.settings.skipChunkPopulation(), "skip_chunk_population", EarthGeneratorSettings::withSkipChunkPopulation, PopulateChunkEvent.Populate.EventType.values())).height();
        y += this.addEntry(new EnumSelectionListEntry<>(this, 5, y, this.entriesWidth, this.settings.skipBiomeDecoration(), "skip_biome_decoration", EarthGeneratorSettings::withSkipBiomeDecoration, DecorateBiomeEvent.Decorate.EventType.values())).height();

        this.entriesHeight = y - VERTICAL_PADDING;

        ScaledResolution scaledRes = new ScaledResolution(Minecraft.getMinecraft());
        this.preview.update(this.width - this.imgSize, VERTICAL_PADDING, this.imgSize - 10, this.height - VERTICAL_PADDING * 4, scaledRes.getScaleFactor(), this.settings.projection());

        double[] bounds = this.settings.projection().bounds();
        this.worldSizeX = Math.abs(Math.round(bounds[2] - bounds[0]));
        this.worldSizeY = Math.abs(Math.round(bounds[3] - bounds[1]));
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

    protected EntryTextField addEntryTextField(int x, int y, int width, int height) {
        EntryTextField textField = new EntryTextField(this.textFields.size(), this.fontRenderer, x, y, width, height);
        this.textFields.add(textField);
        return textField;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format(TerraConstants.MODID + ".gui.header"), this.width >> 1, VERTICAL_PADDING >> 1, 0xFFFFFFFF);

        this.preview.draw();

        { //render list
            int y = VERTICAL_PADDING;

            ScaledResolution scale = new ScaledResolution(Minecraft.getMinecraft());
            int sc = scale.getScaleFactor();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            int height = this.entriesHeight;
            GL11.glScissor(5 * sc, VERTICAL_PADDING * sc, this.entriesWidth * sc, (this.height - VERTICAL_PADDING * 2) * sc);

            GL11.glPushMatrix();
            GL11.glTranslatef(0.0f, -this.deltaY, 0.0f);

            super.drawScreen(mouseX, mouseY + this.deltaY, partialTicks);
            for (Entry entry : this.entries) {
                entry.render(this, 5, y, mouseX, mouseY + this.deltaY, this.entriesWidth);
                y += entry.height();
            }

            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            // scroll bar
            int listHeight = this.entriesHeight;
            int maxListHeight = this.height - VERTICAL_PADDING * 2;
            float viewPort = maxListHeight / (float) listHeight;
            int barHeight = (int) (maxListHeight * viewPort);
            int barY = VERTICAL_PADDING + (int) ((this.deltaY / (float) (listHeight - maxListHeight)) * (maxListHeight - barHeight));

            Gui.drawRect(5 + this.entriesWidth + 5 - 4, VERTICAL_PADDING, 5 + this.entriesWidth + 5, VERTICAL_PADDING + maxListHeight, 0xB0000000);
            if (barHeight > 0 && viewPort < 1) {
                Gui.drawRect(5 + this.entriesWidth + 5 - 4, barY, 5 + this.entriesWidth + 5, barY + barHeight, 0xFFDDDDDD);
            }
        }

        //render text fields
        for (GuiTextField textField : this.textFields) {
            textField.drawTextBox();
        }

        this.fontRenderer.drawSplitString(I18n.format("terraplusplus.gui.world_info"), this.width - this.imgSize + 15, this.height - VERTICAL_PADDING * 2 - 25, this.imgSize - 25, 0xFFFFFFFF);

        this.drawString(this.fontRenderer, I18n.format("terraplusplus.gui.world_size_header"), this.width - this.imgSize + 15, this.height - VERTICAL_PADDING - 30, 0xFFFFFFFF);
        this.drawString(this.fontRenderer, I18n.format("terraplusplus.gui.world_size_numbers", this.worldSizeX, this.worldSizeY), this.width - this.imgSize + 15, this.height - VERTICAL_PADDING - 15, 0xFFFFFFFF);

        for (GuiButton button : this.nonTranslatedButtons) {
            button.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
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
        this.lastClickMoveX = mouseX;
        this.lastClickMoveY = mouseY;
        if (this.doneButton.mousePressed(this.mc, mouseX, mouseY)) {
            this.whenDone.accept(this.settings.toString()); //save settings
            this.mc.displayGuiScreen(this.parent); //exit
        } else if (this.cancelButton.mousePressed(this.mc, mouseX, mouseY)) {
            this.mc.displayGuiScreen(this.parent); //exit without saving
        } else {
            boolean updateQueued = false;
            for (GuiTextField textField : this.textFields) {
                boolean focused = textField.isFocused();
                textField.mouseClicked(mouseX, mouseY + this.deltaY, mouseEvent);
                if (!textField.isFocused() && focused) {
                    updateQueued = true;
                }
            }
            for (GuiButton button : this.buttonList) {
                if (button.mousePressed(this.mc, mouseX, mouseY + this.deltaY)) {
                    updateQueued = true;
                }
            }
            if (updateQueued) {
                this.initGui();
            }
        }

        this.isDraggingScrollbar = mouseX >= 5 + this.entriesWidth + 5 - 4 && mouseX <= 5 + this.entriesWidth + 5
                                   && mouseY >= VERTICAL_PADDING && mouseY <= this.height + VERTICAL_PADDING;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getDWheel();
        if (scroll != 0) {
            this.deltaY = max(min(this.deltaY - scroll / 20, this.entriesHeight - this.height + VERTICAL_PADDING * 2), 0);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.isDraggingScrollbar = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        int dX = mouseX - this.lastClickMoveX;
        int dY = mouseY - this.lastClickMoveY;

        if (this.isDraggingScrollbar) {
            float scaleFactor = (this.height - VERTICAL_PADDING * 2) / (float) this.entriesHeight;
            this.deltaY = max(min(this.deltaY + (int) (dY / scaleFactor), this.entriesHeight - this.height + VERTICAL_PADDING * 2), 0);
        }

        this.lastClickMoveX = mouseX;
        this.lastClickMoveY = mouseY;
    }


    protected interface Entry {
        int height();

        default void render(AdvancedEarthGui gui, int x, int y, int mouseX, int mouseY, int width) {
        }

        default EarthGeneratorSettings touchSettings(EarthGeneratorSettings settings) {
            return settings;
        }
    }

    protected static class ProjectionEntry implements Entry {
        protected final List<SubEntry> entries = new ArrayList<>();

        protected int height = 30;

        public ProjectionEntry(EarthGeneratorSettings settings, AdvancedEarthGui gui, int x, int y, int width) {
            gui.addButton(new GuiButton(0, x + (width >> 1), y, width >> 1, 20, I18n.format(TerraConstants.MODID + ".gui.transformation.add")) {
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
            this.height += 10;
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
        public int height() {
            return this.height;
        }

        @Override
        public void render(AdvancedEarthGui gui, int x, int y, int mouseX, int mouseY, int width) {
            gui.fontRenderer.drawString(I18n.format(TerraConstants.MODID + ".gui.projection"), x, y + (20 - 8) / 2, 0xFFFFFFFF, true);

            y += 30;

            for (SubEntry entry : this.entries) {
                entry.render(gui, x + 10, y, mouseX, mouseY, width - 10);
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
                protected TransformEntry newSubEntry(ProjectionEntry entry, AdvancedEarthGui gui, int x, int y, int width) {
                    return new ParameterizedTransformEntry(this, entry, gui, x, y, width, TerraConstants.MODID + ".gui.transformation.offset", "dx", "dy") {
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
                protected TransformEntry newSubEntry(ProjectionEntry entry, AdvancedEarthGui gui, int x, int y, int width) {
                    return new ParameterizedTransformEntry(this, entry, gui, x, y, width, TerraConstants.MODID + ".gui.transformation.scale", "x", "y") {
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
            },
            rotate {
                @Override
                protected TransformEntry newSubEntry(ProjectionEntry entry, AdvancedEarthGui gui, int x, int y, int width) {
                    return new ParameterizedTransformEntry(this, entry, gui, x, y, width, TerraConstants.MODID + ".gui.transformation.rotate", "by") {
                        @Override
                        public void initFrom(ProjectionTransform in) {
                            if (in instanceof RotateProjectionTransform) {
                                RotateProjectionTransform transform = (RotateProjectionTransform) in;
                                this.textFields[0].setText(String.valueOf(transform.by()));
                            } else {
                                this.textFields[0].setText("1.0");
                            }
                        }

                        @Override
                        protected void appendValue(StringBuilder out, int i) {
                            out.append(Double.parseDouble(this.textFields[i].getText()));
                        }
                    };
                }
            },
            clamp {
                @Override
                protected TransformEntry newSubEntry(ProjectionEntry entry, AdvancedEarthGui gui, int x, int y, int width) {
                    return new ParameterizedTransformEntry(this, entry, gui, x, y, width, TerraConstants.MODID + ".gui.transformation.clamp", "minX", "maxX", "minY", "maxY") {
                        @Override
                        public void initFrom(ProjectionTransform in) {
                            double[] bounds = in instanceof ClampProjectionTransform ? in.bounds() : new double[]{ 0.0d, 0.0d, 1.0d, 1.0d };
                            this.textFields[0].setText(String.valueOf(bounds[0]));
                            this.textFields[1].setText(String.valueOf(bounds[2]));
                            this.textFields[2].setText(String.valueOf(bounds[1]));
                            this.textFields[3].setText(String.valueOf(bounds[3]));
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

            protected TransformEntry newSubEntry(ProjectionEntry entry, AdvancedEarthGui gui, int x, int y, int width) {
                return new TransformEntry(this, entry, gui, x, y, width);
            }
        }

        protected interface SubEntry {
            int height();

            default void render(AdvancedEarthGui gui, int x, int y, int mouseX, int mouseY, int width) {
            }

            void toJson(StringBuilder out);
        }

        protected static class TransformEntry implements SubEntry {
            protected final Transformation transformation;

            protected final EntryButton upButton;
            protected final EntryButton downButton;
            protected final EntryButton removeButton;
            protected final EntryButton nameButton;

            protected Minecraft mc = Minecraft.getMinecraft();

            public TransformEntry(Transformation transformation, ProjectionEntry entry, AdvancedEarthGui gui, int x, int y, int width) {
                this.transformation = transformation;

                this.upButton = gui.addButton(new EntryButton(x, y, 20, "\u25B2") { //up
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
                this.downButton = gui.addButton(new EntryButton(x + 20, y, 20, "\u25BC") { //down
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
                this.removeButton = gui.addButton(new EntryButton(x + 40, y, 20, "\u2716") { //remove
                    @Override
                    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                        if (super.mousePressed(mc, mouseX, mouseY)) {
                            entry.entries.remove(TransformEntry.this);
                            return true;
                        }
                        return false;
                    }
                });

                this.nameButton = gui.addButton(new EntryButton(x + 60, y, width - 60, I18n.format(TerraConstants.MODID + ".gui.transformation." + transformation.name())) {
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

            @Override
            public void render(AdvancedEarthGui gui, int x, int y, int mouseX, int mouseY, int width) {
                this.upButton.y = this.downButton.y = this.removeButton.y = this.nameButton.y = y;
                this.upButton.actuallyDrawButton(this.mc, mouseX, mouseY);
                this.downButton.actuallyDrawButton(this.mc, mouseX, mouseY);
                this.removeButton.actuallyDrawButton(this.mc, mouseX, mouseY);
                this.nameButton.actuallyDrawButton(this.mc, mouseX, mouseY);
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
            protected final EntryTextField[] textFields;

            public ParameterizedTransformEntry(Transformation transformation, ProjectionEntry entry, AdvancedEarthGui gui, int x, int y, int width, String fieldName, String... paramNames) {
                super(transformation, entry, gui, x, y, width);

                this.fieldName = fieldName;
                this.paramNames = paramNames;

                int maxLen = Arrays.stream(paramNames)
                                     .map(s -> fieldName + '.' + s)
                                     .map(I18n::format)
                                     .mapToInt(gui.fontRenderer::getStringWidth)
                                     .max().getAsInt() + 5;

                this.textFields = new EntryTextField[paramNames.length];
                for (int i = 0; i < paramNames.length; i++) {
                    this.textFields[i] = gui.addEntryTextField(x + maxLen, y + super.height() + 2 + i * 24, width - maxLen - 2, 20);
                }

                this.initFrom(null);
            }

            @Override
            public int height() {
                return super.height() + 2 + this.textFields.length * 24;
            }

            @Override
            public void render(AdvancedEarthGui gui, int x, int y, int mouseX, int mouseY, int width) {
                super.render(gui, x, y, mouseX, mouseY, width);
                for (int i = 0; i < this.paramNames.length; i++) {
                    gui.fontRenderer.drawString(I18n.format(this.fieldName + '.' + this.paramNames[i]), x, y + super.height() + 2 + i * 24 + (20 - 8) / 2, -1, true);
                }
                for (int i = 0; i < this.paramNames.length; i++) {
                    this.textFields[i].y = y + super.height() + 2 + i * 24;
                    this.textFields[i].actuallyDrawTextBox();
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
            protected final EntryTextField[] textFields;
            protected final EntryButton button;
            protected final Minecraft mc = Minecraft.getMinecraft();

            public RootEntry(GeographicProjection projection, AdvancedEarthGui gui, int x, int y, int width) {
                String projectionName = GlobalParseRegistries.PROJECTIONS.inverse().get(projection.getClass());
                this.initialIndex = this.index = PArrays.linearSearch(PROJECTION_NAMES, projectionName);

                this.button = gui.addButton(new EntryButton(x, y, width, I18n.format(this.fieldName = TerraConstants.MODID + ".gui.projection." + projectionName)) {
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

                this.textFields = new EntryTextField[this.properties.size()];
                int i = 0;
                for (Map.Entry<String, Object> entry : this.properties.entrySet()) {
                    this.textFields[i] = gui.addEntryTextField(x + maxLen, y + 20 + 2 + i * 24, width - maxLen - 2, 20);
                    this.textFields[i].setText(Objects.toString(entry.getValue()));
                    i++;
                }
            }

            @Override
            public int height() {
                return 20 + 2 + this.textFields.length * 24;
            }

            @Override
            public void render(AdvancedEarthGui gui, int x, int y, int mouseX, int mouseY, int width) {
                int i = 0;
                for (String s : this.properties.keySet()) {
                    gui.fontRenderer.drawString(I18n.format(this.fieldName + '.' + s), x, y + 20 + 2 + i * 24 + (20 - 8) / 2, -1, true);
                    i++;
                }
                this.button.y = y;
                for (int j = 0; j < this.textFields.length; j++) {
                    this.textFields[j].y = y + 20 + 2 + j * 24;
                    this.textFields[j].actuallyDrawTextBox();
                }
                this.button.actuallyDrawButton(this.mc, mouseX, mouseY);
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

        public ToggleEntry(AdvancedEarthGui gui, int x, int y, int width, boolean value, String name, BiFunction<EarthGeneratorSettings, Boolean, EarthGeneratorSettings> touch) {
            this.touch = touch;
            this.value = value;

            gui.addButton(new GuiButton(0, x, y, width, 20, I18n.format(TerraConstants.MODID + ".gui." + name) + ": " + I18n.format("options." + (value ? "on" : "off"))) {
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

        public CWGEntry(EarthGeneratorSettings settings, AdvancedEarthGui gui, int x, int y, int width) {
            int text = gui.fontRenderer.getStringWidth(I18n.format(TerraConstants.MODID + ".gui.cwg")) + 5;
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
        public void render(AdvancedEarthGui gui, int x, int y, int mouseX, int mouseY, int width) {
            gui.fontRenderer.drawString(I18n.format(TerraConstants.MODID + ".gui.cwg"), x, y + (20 - 8) / 2, 0xFFFFFFFF, true);
        }

        @Override
        public EarthGeneratorSettings touchSettings(EarthGeneratorSettings settings) {
            return settings.withCwg(this.text);
        }
    }

    /**
     * Did you have enough hacky stuff yet ?
     */
    private static class EntryButton extends GuiButton {

        public EntryButton(int x, int y, int width, String buttonText) {
            super(0, x, y, width, 20, buttonText);
        }

        /**
         * This is so we control when the buttons are drawn
         */
        @Override
        @Deprecated
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            // No-op
        }

        /**
         * This is so we control when the buttons are drawn
         */
        public void actuallyDrawButton(Minecraft mc, int mouseX, int mouseY) {
            super.drawButton(mc, mouseX, mouseY, 0);
        }


    }

    /**
     * Did you have enough hacky stuff yet ?
     */
    private static class EntryTextField extends GuiTextField {

        public EntryTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {
            super(componentId, fontrendererObj, x, y, width, height);
        }

        @Override
        @Deprecated
        public void drawTextBox() {
            // no-op
        }

        public void actuallyDrawTextBox() {
            super.drawTextBox();
        }


    }

    public static class EnumSelectionListEntry<T extends Enum<T>> implements Entry {
        protected final BiFunction<EarthGeneratorSettings, Set<T>, EarthGeneratorSettings> touch;
        protected final String name;
        protected Set<T> values;
        protected int height = 20;

        public EnumSelectionListEntry(AdvancedEarthGui gui, int x, int y, int width, Set<T> values, String name, BiFunction<EarthGeneratorSettings, Set<T>, EarthGeneratorSettings> touch, T[] allValues) {
            this.touch = touch;
            this.name = name;
            this.values = EnumSet.copyOf(values);

            for (T value : allValues) {
                gui.addButton(new GuiButton(0, x + 2, y + this.height, width - 2, 20, value + ": " + I18n.format("options." + (this.values.contains(value) ? "off" : "on"))) {
                    @Override
                    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                        if (super.mousePressed(mc, mouseX, mouseY)) {
                            if (!EnumSelectionListEntry.this.values.add(value)) {
                                EnumSelectionListEntry.this.values.remove(value);
                            }
                            return true;
                        }
                        return false;
                    }
                });
                this.height += 20;
            }
        }

        @Override
        public int height() {
            return this.height;
        }

        @Override
        public void render(AdvancedEarthGui gui, int x, int y, int mouseX, int mouseY, int width) {
            gui.fontRenderer.drawString(I18n.format(TerraConstants.MODID + ".gui." + this.name), x, y + (20 - 8) / 2, 0xFFFFFFFF, true);
        }

        @Override
        public EarthGeneratorSettings touchSettings(EarthGeneratorSettings settings) {
            return this.touch.apply(settings, this.values);
        }
    }

    protected class ProjectionPreview {
        private int previewX, previewY, previewWidth, previewHeight, scaling;
        private volatile boolean finish;
        private volatile boolean reset;
        private DynamicTexture previewTexture;
        private GeographicProjection projection;
        private final AtomicBoolean working = new AtomicBoolean(false);
        private final AtomicBoolean textureNeedsUpdate = new AtomicBoolean(false);
        private final int[] src;
        private volatile int[] dst;
        private final Thread worker;

        public ProjectionPreview() {
            this.worker = new Thread(this::work);
            this.worker.setDaemon(true);
            this.worker.setName("Projection preview");
            this.worker.start();
            try {
                Thread.sleep(10); // Just make sure we let the worker time to initialize
            } catch (InterruptedException e) {
                TerraMod.LOGGER.catching(e);
            }
            this.src = SRC_CACHE.get();
        }

        private void work() {
            while (!this.finish) {

                // Wait for the client thread to tell us to start
                synchronized (this.working) {
                    try {
                        this.working.wait();
                    } catch (InterruptedException e) {
                        TerraMod.LOGGER.catching(e);
                    }
                    this.working.set(true);
                }

                // Actually do the work
                this.projectTexture(this.dst, this.previewWidth * this.scaling, this.previewHeight * this.scaling, this.projection);
                synchronized (this.textureNeedsUpdate) {
                    this.textureNeedsUpdate.set(true);
                }

                // Let the client thread know we are done
                synchronized (this.working) {
                    this.working.set(false);
                    this.working.notify();
                }

            }
        }

        public void update(int x, int y, int width, int height, int scaling, @NonNull GeographicProjection proj) {
            this.previewX = x;
            this.previewY = y;
            this.previewWidth = width;
            this.previewHeight = height;
            this.projection = proj;
            this.scaling = scaling;

            // Ensure the worker stops its task
            this.reset = true;
            synchronized (this.working) {
                if (this.working.get()) {
                    try {
                        this.working.wait();
                    } catch (InterruptedException e) {
                        TerraMod.LOGGER.catching(e);
                    }
                }
            }

            if (this.previewTexture != null) {
                this.previewTexture.deleteGlTexture();
            }
            this.previewTexture = new DynamicTexture(width * scaling, height * scaling);
            this.dst = this.previewTexture.getTextureData();
            Arrays.fill(this.dst, 0); //fill with transparent pixels

            // Let the worker start the new task
            this.reset = false;
            synchronized (this.working) {
                this.working.notify();
            }

        }

        public void finish() {
            this.reset = true;
            this.finish = true;

            // If the worker was waiting, notify it so it exists
            synchronized (this.working) {
                if (!this.working.get()) {
                    this.working.notify();
                }
            }
        }

        private void projectTexture(int[] dst, int width, int height, GeographicProjection projection) {

            // Scale should be able to fit whole earth inside texture
            double[] bounds = projection.bounds();

            double minX = min(bounds[0], bounds[2]);
            double maxX = max(bounds[0], bounds[2]);
            double minY = min(bounds[1], bounds[3]);
            double maxY = max(bounds[1], bounds[3]);
            double dx = maxX - minX;
            double dy = maxY - minY;
            double scale = max(dx, dy) / (double) Math.min(width, height);

            // Actually set map data
            for (int yi = 0; yi < height && !this.reset; yi++) {
                double y = yi * scale + minY;
                if (y <= minY || y >= maxY) { //skip entire row if y value out of projection bounds
                    continue;
                }

                for (int xi = 0; xi < width; xi++) {
                    double x = xi * scale + minX;
                    if (x <= minX || x >= maxX) { //sample out of bounds, skip it
                        continue;
                    }

                    try {
                        double[] projected = projection.toGeo(x, y);
                        int xPixel = (int) (((projected[0] + 180.0d) * (SRC_W / 360.0d)));
                        int yPixel = (int) (((projected[1] + 90.0d) * (SRC_H / 180.0d)));
                        if (xPixel < 0 || xPixel >= SRC_W || yPixel < 0 || yPixel >= SRC_H) { //projected sample is out of bounds
                            continue;
                        }

                        dst[yi * width + xi] = this.src[(SRC_H - yPixel - 1) * SRC_W + xPixel];
                    } catch (OutOfProjectionBoundsException ignored) {
                        //sample out of bounds, skip it
                    }
                }
                synchronized (this.textureNeedsUpdate) {
                    this.textureNeedsUpdate.set(true);
                }
            }

            if (0 >= minX && 0 <= maxX && 0 >= minY && 0 <= maxY) {
                int dotSize = 3;
                int x = (int) Math.round(-minX / scale);
                int y = (int) Math.round(-minY / scale);
                for (int xi = x - dotSize; xi < x + dotSize + 1; xi++) {
                    if (xi < 0 || xi >= width) {
                        continue;
                    }
                    for (int yi = y - dotSize; yi < y + dotSize + 1; yi++) {
                        if (yi < 0 || yi >= height) {
                            continue;
                        }
                        dst[yi * width + xi] = 0xFFFF00FF;
                    }
                }
            }

            synchronized (this.textureNeedsUpdate) {
                this.textureNeedsUpdate.set(true);
            }
        }

        public void draw() {
            synchronized (this.textureNeedsUpdate) {
                if (this.textureNeedsUpdate.get()) {
                    this.previewTexture.updateDynamicTexture();
                    this.textureNeedsUpdate.set(false);
                }
                GlStateManager.bindTexture(this.previewTexture.getGlTextureId());
                drawScaledCustomSizeModalRect(this.previewX, this.previewY, 0, 0, this.previewWidth, this.previewHeight, this.previewWidth, this.previewHeight, this.previewWidth, this.previewHeight);
            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(DIRECTIONS_TEXTURE);
            drawScaledCustomSizeModalRect(this.previewX + this.previewWidth - 64, this.previewY, 0, 0, 64, 64, 64, 64, 64, 64);
        }

    }
}
