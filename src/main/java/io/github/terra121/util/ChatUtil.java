package io.github.terra121.util;

import io.github.terra121.TerraConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ChatUtil {
    public static ITextComponent title() {
        return new TextComponentString(TerraConstants.prefix.replace("&","\u00A7"));
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
        for(Object o : objects) {
            if(o instanceof ITextComponent) {
                if(builder != null) {
                    textComponent.appendSibling(new TextComponentString(builder.toString()));
                    builder = null;
                }

                textComponent.appendSibling((ITextComponent) o);
            } else {
                if(builder == null) builder = new StringBuilder();
                builder.append(o);
            }
        }

        if(builder != null)
            textComponent.appendSibling(new TextComponentString(builder.toString()));
        return textComponent;
    }

    public static ITextComponent getNotCC() {
        return titleAndCombine(TextFormatting.RED, TranslateUtil.translate("terra121.error.notcc"));
    }

    public static ITextComponent getNotTerra() {
        return titleAndCombine(TextFormatting.RED, TranslateUtil.translate("terra121.error.noterra"));
    }

    public static ITextComponent getNoPermission() {
        return titleAndCombine(TextFormatting.RED, "You do not have permission to use this command");
    }

    public static ITextComponent getPlayerOnly() {
        return titleAndCombine(TextFormatting.RED, TranslateUtil.translate("terra121.error.playeronly"));
    }

}
