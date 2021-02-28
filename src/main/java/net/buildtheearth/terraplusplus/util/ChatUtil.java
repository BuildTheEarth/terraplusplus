package net.buildtheearth.terraplusplus.util;

import net.buildtheearth.terraplusplus.TerraConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/**
 * A set of chat utilities for forge
 *
 * @author Noah Husby
 */
public class ChatUtil {
    public static ITextComponent title() {
        return new TextComponentString(TerraConstants.CHAT_PREFIX.replace("&", "\u00A7"));
    }

    public static ITextComponent titleAndCombine(Object... objects) {
        return combine(true, objects);
    }

    public static ITextComponent combine(Object... objects) {
        return combine(false, objects);
    }

    public static ITextComponent combine(boolean title, Object... objects) {
        ITextComponent textComponent = title ? title() : new TextComponentString("");
        StringBuilder builder = null;
        TextFormatting lastFormat = null;
        for (Object o : objects) {
            if (o instanceof ITextComponent) {
                if (builder != null) {
                    textComponent.appendSibling(new TextComponentString(builder.toString()));
                    builder = null;
                }

                ITextComponent component = (ITextComponent) o;
                if (component.getStyle().getColor() == null && lastFormat != null) {
                    component.setStyle(new Style().setColor(lastFormat));
                }

                textComponent.appendSibling(component);
            } else {
                if (o instanceof TextFormatting) {
                    lastFormat = (TextFormatting) o;
                }
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(o);
            }
        }

        if (builder != null) {
            textComponent.appendSibling(new TextComponentString(builder.toString()));
        }
        return textComponent;
    }

    public static ITextComponent getNotCC() {
        return titleAndCombine(TextFormatting.RED, TranslateUtil.translate(TerraConstants.MODID + ".error.notcc"));
    }

    public static ITextComponent getNotTerra() {
        return titleAndCombine(TextFormatting.RED, TranslateUtil.translate(TerraConstants.MODID + ".error.noterra"));
    }

    public static ITextComponent getNoPermission() {
        return titleAndCombine(TextFormatting.RED, "You do not have permission to use this command");
    }

    public static ITextComponent getPlayerOnly() {
        return titleAndCombine(TextFormatting.RED, TranslateUtil.translate(TerraConstants.MODID + ".error.playeronly"));
    }
}
