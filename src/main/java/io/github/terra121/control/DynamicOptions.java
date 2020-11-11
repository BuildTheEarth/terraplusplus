package io.github.terra121.control;

import io.github.terra121.TerraMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class DynamicOptions extends GuiSlot {

    private Element[] elements;
    private Handler handler;

    public DynamicOptions(Minecraft mcIn, int width, int height, int top, int bottom, int slotsize, Handler handler, Element[] elems) {
        super(mcIn, width, height, top, bottom, slotsize);
        this.elements = elems;
        this.handler = handler;
    }

    @Override
    protected int getSize() {
        return this.elements.length;
    }

    /**
     * The element in the slot that was clicked, boolean for whether it was double clicked or not
     * This don't work idk why, so using mouseClicked
     */
    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
    }

    //click the proper button
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.isMouseYWithinSlotBounds(mouseY)) {
            int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);

            if (i >= 0) {
                this.elements[i].click(this.mc, mouseX, mouseY, mouseButton);
                if (this.handler != null) {
                    this.handler.onDynOptClick(this.elements[i]);
                }
            }
        }
    }

    public void update() {
        for (Element e : this.elements) {
            e.update();
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        for (Element e : this.elements) {
            e.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Returns true if the element passed in is currently selected
     */
    @Override
    protected boolean isSelected(int slotIndex) {
        return false;
    }

    @Override
    protected void drawBackground() {
        //EarthGui.this.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
        this.elements[slotIndex].draw(this.mc, xPos, yPos, heightIn, mouseXIn, mouseYIn, partialTicks);
    }

    public interface Handler {
        void onDynOptClick(Element elem);
    }

    public abstract static class Element {
        public abstract void draw(Minecraft mc, int x, int y, int height, int mouseX, int mouseY, float partialTicks);

        public void click(Minecraft mc, int mouseX, int mouseY, int mouseEvent) {
        }

        public void keyTyped(char typedChar, int keyCode) {
        }

        public void update() {
        }
    }

    public static class TextFieldElement extends Element {

        public GuiTextField gui;
        int id;
        public String defaultText;
        public Field outf;
        public Object outO;

        public TextFieldElement(int id, Field outfield, Object outO, String defaultText) {
            this.defaultText = defaultText;
            this.gui = new GuiTextField(this.id, Minecraft.getMinecraft().fontRenderer, 0, 0, 200, 20);
            this.gui.setMaxStringLength(1000); //TODO Make that as long as it needs to be
            this.gui.setText(this.defaultText);
            this.id = id;
            this.outf = outfield;
            this.outO = outO;
        }

        @Override
        public void click(Minecraft mc, int mouseX, int mouseY, int mouseEvent) {
            this.gui.mouseClicked(mouseX, mouseY, mouseEvent);
        }

        @Override
        public void draw(Minecraft mc, int x, int y, int height, int mouseX, int mouseY, float partialTicks) {
            this.gui.x = x;
            this.gui.y = y;
            this.gui.height = height > 20 ? 20 : height;
            this.gui.width = 200;
            this.gui.drawTextBox();
        }

        public String getText() {
            return (this.gui.getText());
        }

        @Override
        public void update() {
            this.gui.updateCursorCounter();
            try {
                this.outf.set(this.outO, this.gui.getText());
            } catch (IllegalAccessException e) {
                TerraMod.LOGGER.error("This should never happen, but set reflection error");
                e.printStackTrace();
            }
        }

        @Override
        public void keyTyped(char typedChar, int keyCode) {
            this.gui.textboxKeyTyped(typedChar, keyCode);
        }

    }

    public static class CycleButtonElement<E> extends Element {
        public GuiButton gui;
        public E[] options;
        public int current;
        Function<E, String> tostring;
        Field outf;
        Object outo;

        public CycleButtonElement(int id, E[] options, Field outfield, Object outobject, Function<E, String> tostring) {
            this.outo = outobject;
            this.outf = outfield;
            this.options = options;
            this.tostring = tostring;

            try {
                this.current = Arrays.asList(options).indexOf(outfield.get(outobject));
            } catch (IllegalAccessException e) {
                TerraMod.LOGGER.error("This should never happen, but get reflection error");
                e.printStackTrace();
            }

            this.gui = new GuiButton(id, 0, 0, tostring.apply(options[this.current]));
        }

        @Override
        public void click(Minecraft mc, int mouseX, int mouseY, int mouseEvent) {
            this.current++;
            if (this.current >= this.options.length) {
                this.current = 0;
            }

            try {
                this.outf.set(this.outo, this.options[this.current]);
            } catch (IllegalAccessException e) {
                TerraMod.LOGGER.error("This should never happen, but set reflection error");
                e.printStackTrace();
            }

            this.gui.displayString = this.tostring.apply(this.options[this.current]);
        }

        @Override
        public void draw(Minecraft mc, int x, int y, int height, int mouseX, int mouseY, float partialTicks) {
            this.gui.height = height > 20 ? 20 : height;
            this.gui.x = x;
            this.gui.y = y;
            this.gui.drawButton(mc, mouseX, mouseY, partialTicks);
        }
    }

    //quick wrapper for a yes or no toggle
    public static class ToggleElement extends CycleButtonElement<Boolean> {
        public ToggleElement(int id, String name, Field outfield, Object outobject, Consumer<Boolean> notify) {
            super(id, new Boolean[]{ false, true }, outfield, outobject, b -> {
                if (notify != null) {
                    notify.accept(b);
                }
                return name + ": " + (b ? I18n.format("options.on") : I18n.format("options.off"));
            });
        }
    }
}
