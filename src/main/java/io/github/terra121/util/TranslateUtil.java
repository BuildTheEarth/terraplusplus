package io.github.terra121.util;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class TranslateUtil {
    public static String translate(String key) {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return key;
        return new TextComponentTranslation(key).getFormattedText();
    }
}
