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
        return combine(title(), objects);
    }

    public static ITextComponent combine(Object... objects) {
        ITextComponent textComponent = new TextComponentString("");
        for(Object o : objects) {
            if(o instanceof ITextComponent) {
                textComponent.appendSibling((ITextComponent) o);
            } else if(o instanceof String) {
                textComponent.appendSibling(new TextComponentString((String) o));
            } else if(o instanceof TextFormatting) {
                textComponent.appendSibling(new TextComponentString(((TextFormatting) o).toString()));
            } else {
                textComponent.appendSibling(new TextComponentString(String.valueOf(o)));
            }
        }

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
