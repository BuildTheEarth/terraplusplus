package io.github.terra121;

import io.github.terra121.util.TranslateUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TerraConstants {
    public static final String prefix = "&2&lT++ &8&l> ";
    public static final String modID = "terra121";

    public static final String defaultCommandNode = modID + ".command.";
    public static final String controlCommandNode = modID + ".commands.";
    public static final String adminCommandNode = modID + ".admin";

    public static final String version = "1.0";
    public static class TextConstants {
        // Live update not final
        public static ITextComponent getNotCC() {
            return title(new TextComponentString(TextFormatting.RED + TranslateUtil.translate("terra121.error.notcc")));
        }

        public static ITextComponent getNotTerra() {
            return title(new TextComponentString(TextFormatting.RED + TranslateUtil.translate("terra121.error.noterra")));
        }

        public static ITextComponent getNoPermission() {
            return title(new TextComponentString(TextFormatting.RED + "You do not have permission to use this command"));
        }

        public static ITextComponent getPlayerOnly() {
            return title(new TextComponentString(TextFormatting.RED + TranslateUtil.translate("terra121.error.playeronly")));
        }

        public static ITextComponent title() {
            return new TextComponentString(TerraConstants.prefix.replace("&","\u00A7"));
        }

        public static ITextComponent title(ITextComponent component) {
            return title().appendSibling(component);
        }

        public static ITextComponent title(String s) {
            return title(new TextComponentString(s));
        }
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
