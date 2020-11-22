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
        public static final ITextComponent notCC = ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.notcc"), TextFormatting.RED));
        public static final ITextComponent notTerra = ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.notterra"), TextFormatting.RED));
        public static final ITextComponent noPermission = ChatHelper.makeTextComponent(new TextElement("You do not have permission to use this command", TextFormatting.RED));
        public static final ITextComponent playerOnly = ChatHelper.makeTitleTextComponent(new TextElement(TranslateUtil.translate("terra121.error.playeronly"), TextFormatting.RED));
    }
    
	/**
	 * Earth's circumference around the equator, in meters.
	 */
    public static final double EARTH_CIRCUMFERENCE = 40075017;
    
    /**
     * Earth's circumference around the poles, in meters.
     */
    public static final double EARTH_POLAR_CIRCUMFERENCE = 40008000;
    
}
