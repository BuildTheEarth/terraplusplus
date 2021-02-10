package net.buildtheearth.terraplusplus.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TranslateUtil {
    public static ITextComponent translate(String key) {
        if(FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) return new TextComponentTranslation(key);
        return new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocal(key));
    }

    public static ITextComponent format(String key, Object... args) {
        if(FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) return new TextComponentTranslation(key, args);
        return new TextComponentString(I18n.translateToLocalFormatted(key, args));
    }
}
