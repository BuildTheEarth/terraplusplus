package io.github.terra121.chat;

import net.minecraft.util.text.TextFormatting;

public class TextElement {
    public final String text;
    public final TextFormatting color;
    public final boolean bold;

    public TextElement(String text, TextFormatting color) {
        this(text, color, false);
    }

    public TextElement(String text, TextFormatting color, boolean bold) {
        this.text = text;
        this.color = color;
        this.bold = bold;
    }
}
