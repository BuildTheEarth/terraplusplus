package io.github.terra121.util;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class TranslateUtil {
    public static String translate(String key) {
        if(FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) return I18n.format(key, new Object[0]);
        return new TextComponentTranslation(key).getFormattedText();
    }

    public static String format(String key, Object... args) {
        if(FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) return I18n.format(key, args);
        return String.format(translate(key), args);
    }
}
