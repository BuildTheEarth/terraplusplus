package io.github.terra121.generator.populate;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.EarthGeneratorSettings;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.NonNull;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Map;
import java.util.Random;

/**
 * Implementation of {@link IEarthPopulator} which delegates tasks to ordinary cubic populators for the biome in the given cube.
 *
 * @author DaPorkchop_
 */
public class BiomeDecorationPopulator implements IEarthPopulator {
    protected final Map<Biome, ICubicPopulator> populators = new Reference2ObjectOpenHashMap<>(ForgeRegistries.BIOMES.getKeys().size());

    public BiomeDecorationPopulator(@NonNull EarthGeneratorSettings settings) {
        for (Biome biome : ForgeRegistries.BIOMES) {
            this.populators.put(biome, CubicBiome.getCubic(biome).getDecorator(settings.customCubic()));
        }
    }

    @Override
    public void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData data) {
        this.populators.get(biome).generate(world, random, pos, biome);
    }
}
