package net.buildtheearth.terraplusplus.generator.populate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
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
@JsonDeserialize
public final class PopulatorBiomeDecoration implements IEarthPopulator {
    protected transient final Map<Biome, ICubicPopulator> populators = new Reference2ObjectOpenHashMap<>(ForgeRegistries.BIOMES.getKeys().size());
    protected transient volatile boolean initialized = false;

    @Override
    public void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData[] datas, EarthGeneratorSettings settings) {
        if (!this.initialized) {
            this.init(settings);
        }

        this.populators.get(biome).generate(world, random, pos, biome);
    }

    private synchronized void init(EarthGeneratorSettings settings) {
        if (this.initialized) {
            return;
        }
        this.initialized = true;

        for (Biome biome : ForgeRegistries.BIOMES) {
            this.populators.put(biome, CubicBiome.getCubic(biome).getDecorator(settings.customCubic()));
        }
    }
}
