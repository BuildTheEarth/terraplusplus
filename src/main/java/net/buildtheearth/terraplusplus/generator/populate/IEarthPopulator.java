package net.buildtheearth.terraplusplus.generator.populate;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

/**
 * A cube populator for earth terrain data.
 *
 * @author DaPorkchop_
 * @see ICubicPopulator
 */
@FunctionalInterface
public interface IEarthPopulator {
    /**
     * @param data the {@link CachedChunkData} for the column
     * @see ICubicPopulator#generate(World, Random, CubePos, Biome)
     */
    void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData data);
}
