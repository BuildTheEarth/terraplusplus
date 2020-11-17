package io.github.terra121;

import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class TerraConstants {
    public static final String prefix = "&9&lTerra++ &8&l> ";
    public static final String version = "1.0";
    public static class TextConstants {
        public static final ITextComponent notCC = ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.notcc"), TextFormatting.RED));
        public static final ITextComponent notTerra = ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.notterra"), TextFormatting.RED));
        public static final ITextComponent noPermission = ChatHelper.makeTextComponent(new TextElement("You don't have permissions to run this command", TextFormatting.DARK_RED));
        public static final ITextComponent playerOnly = ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.playeronly"), TextFormatting.RED));
    }
}
