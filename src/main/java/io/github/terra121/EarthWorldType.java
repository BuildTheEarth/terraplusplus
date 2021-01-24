package io.github.terra121;

import io.github.opencubicchunks.cubicchunks.api.util.IntRange;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldType;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.terra121.control.EarthGui;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.generator.EarthGeneratorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EarthWorldType extends WorldType implements ICubicWorldType {
	
    public static EarthWorldType create() {
        return new EarthWorldType();
    }

    public EarthWorldType() {
        super("EarthCubic");
    }

    @Override
    public ICubeGenerator createCubeGenerator(World world) {
        return new EarthGenerator(world);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        return EarthGeneratorSettings.parse(world.getWorldInfo().getGeneratorOptions()).biomeProvider();
    }

    @Override
    public IntRange calculateGenerationHeightRange(WorldServer world) {
        return new IntRange(-12000, 9000);
    }

    @Override
    public boolean hasCubicGeneratorForWorld(World w) {
        return w.provider instanceof WorldProviderSurface; // an even more general way to check if it's overworld (need custom providers)
    }

    @Override
    public boolean isCustomizable() {
        return true;
    }

    @Override
    public float getCloudHeight() {
        return 5000;
    }

    @Override
    public double voidFadeMagnitude() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(Minecraft mc, GuiCreateWorld guiCreateWorld) {
        mc.displayGuiScreen(new EarthGui(guiCreateWorld, mc));
    }
}
