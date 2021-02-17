package net.buildtheearth.terraplusplus.generator.populate;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubeGeneratorsRegistry;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.event.PopulateCubeEvent;
import lombok.experimental.UtilityClass;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;

import java.util.Random;

/**
 * {@link IEarthPopulator}s for inter-mod compatibility.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class CompatibilityEarthPopulators {
    /**
     * Fires {@link PopulateCubeEvent.Pre}.
     */
    public IEarthPopulator cubePopulatePre() {
        return (world, random, pos, biome, datas) ->
                MinecraftForge.EVENT_BUS.post(new PopulateCubeEvent.Pre(world, random, pos.getX(), pos.getY(), pos.getZ(), false));
    }

    /**
     * Fires {@link PopulateCubeEvent.Post}.
     */
    public IEarthPopulator cubePopulatePost() {
        return (world, random, pos, biome, datas) ->
                MinecraftForge.EVENT_BUS.post(new PopulateCubeEvent.Post(world, random, pos.getX(), pos.getY(), pos.getZ(), false));
    }

    /**
     * Calls {@link CubeGeneratorsRegistry#generateWorld(World, Random, CubePos, Biome)}.
     */
    public IEarthPopulator cubeGeneratorsRegistry() {
        return (world, random, pos, biome, datas) -> CubeGeneratorsRegistry.generateWorld(world, random, pos, biome);
    }
}
