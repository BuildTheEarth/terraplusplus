package net.buildtheearth.terraplusplus.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
public class TerraPlusPlusMixinLoader implements IFMLLoadingPlugin {
    public TerraPlusPlusMixinLoader() {
        try {
            Class.forName("org.spongepowered.asm.launch.MixinBootstrap");
            MixinBootstrap.init();
            Mixins.addConfiguration("terraplusplus.mixins.json");
        } catch (ClassNotFoundException ignored) {
            //this means cubic chunks isn't there, let forge show missing dependency screen
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
