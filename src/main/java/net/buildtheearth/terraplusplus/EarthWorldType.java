package net.buildtheearth.terraplusplus;

import io.github.opencubicchunks.cubicchunks.api.util.IntRange;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldType;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import net.buildtheearth.terraplusplus.control.PresetEarthGui;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
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
        return EarthGeneratorSettings.forWorld((WorldServer) world).biomeProvider();
    }

    @Override
    public IntRange calculateGenerationHeightRange(WorldServer world) {
        return new IntRange(-12000, 9000);
    }

    @Override
    public boolean hasCubicGeneratorForWorld(World w) {
        return w.provider instanceof WorldProviderSurface
               || (TerraConfig.dimension.overrideNetherGeneration && w.provider instanceof WorldProviderHell)
               || (TerraConfig.dimension.overrideEndGeneration && w.provider instanceof WorldProviderEnd);
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
    public double getHorizon(World world) {
        return Integer.MIN_VALUE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(Minecraft mc, GuiCreateWorld guiCreateWorld) {
        String sanitized = EarthGeneratorSettings.parse(guiCreateWorld.chunkProviderSettingsJson).toString();
        mc.displayGuiScreen(new PresetEarthGui(guiCreateWorld, sanitized, s -> guiCreateWorld.chunkProviderSettingsJson = s));
    }
}
