package io.github.terra121.generator.populate;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.terra121.generator.cache.CachedChunkData;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

/**
 * A cube populator for earth terrain data.
 *
 * @author DaPorkchop_
 * @see ICubicPopulator
 */
public interface IEarthPopulator {
    /**
     * @param data the {@link CachedChunkData} for the column
     * @see ICubicPopulator#generate(World, Random, CubePos, Biome)
     */
    void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData data);
}
