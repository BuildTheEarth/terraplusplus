package io.github.terra121;

import io.github.terra121.chat.ChatHelper;
import io.github.terra121.chat.TextElement;
import io.github.terra121.util.TranslateUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class TerraConstants {
    public static final String prefix = "&9&lTerra++ &8&l> ";
    public static final String modID = "terra121";

    public static final String defaultCommandNode = modID + ".command.";
    public static final String controlCommandNode = modID + ".commands.";
    public static final String adminCommandNode = modID + ".admin";

    public static final String version = "1.0";
    public static class TextConstants {
        // Live update not final
        public static ITextComponent getNotCC() {
            return ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.notcc"), TextFormatting.RED));
        }

        public static ITextComponent getNotTerra() {
            return ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.notterra"), TextFormatting.RED));
        }

        public static ITextComponent getNoPermission() {
            return ChatHelper.makeTextComponent(new TextElement("You do not have permission to use this command", TextFormatting.RED));
        }

        public static ITextComponent getPlayerOnly() {
            return ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.playeronly"), TextFormatting.RED));
        }
    }
}
