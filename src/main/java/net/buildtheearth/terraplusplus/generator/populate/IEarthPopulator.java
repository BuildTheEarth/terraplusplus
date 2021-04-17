package net.buildtheearth.terraplusplus.generator.populate;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A cube populator for earth terrain data.
 *
 * @author DaPorkchop_
 * @see ICubicPopulator
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonTypeIdResolver(IEarthPopulator.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface IEarthPopulator {
    /**
     * @param datas the {@link CachedChunkData} for the 2x2 column area being populated
     * @see ICubicPopulator#generate(World, Random, CubePos, Biome)
     */
    void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData[] datas, EarthGeneratorSettings settings);

    /**
     * @return this {@link IEarthPopulator}'s type ID
     */
    default String typeId() {
        String typeId = GlobalParseRegistries.GENERATOR_SETTINGS_POPULATOR.inverse().get(this.getClass());
        checkState(typeId != null, "unknown IEarthBiomeFilter implementation: %s", this.getClass());
        return typeId;
    }

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<IEarthPopulator> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.GENERATOR_SETTINGS_POPULATOR);
        }
    }
}
