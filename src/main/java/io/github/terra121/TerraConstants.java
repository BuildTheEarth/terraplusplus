package io.github.terra121;

import com.google.gson.Gson;
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

    public static final Gson GSON = new Gson();

    /**
     * Earth's circumference around the equator, in meters.
     */
    public static final double EARTH_CIRCUMFERENCE = 40075017;

    /**
     * Earth's circumference around the poles, in meters.
     */
    public static final double EARTH_POLAR_CIRCUMFERENCE = 40008000;
}
