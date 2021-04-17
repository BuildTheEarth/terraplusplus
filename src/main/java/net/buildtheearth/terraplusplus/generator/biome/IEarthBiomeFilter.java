package net.buildtheearth.terraplusplus.generator.biome;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.generator.ChunkBiomesBuilder;
import net.buildtheearth.terraplusplus.generator.IEarthAsyncPipelineStep;
import net.buildtheearth.terraplusplus.util.ImmutableCompactArray;
import net.minecraft.world.biome.Biome;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonTypeIdResolver(IEarthBiomeFilter.TypeIdResolver.class)
@JsonDeserialize
public interface IEarthBiomeFilter<D> extends IEarthAsyncPipelineStep<D, ImmutableCompactArray<Biome>, ChunkBiomesBuilder> {
    /**
     * @return this {@link IEarthBiomeFilter}'s type ID
     */
    default String typeId() {
        String typeId = GlobalParseRegistries.GENERATOR_SETTINGS_BIOME_FILTER.inverse().get(this.getClass());
        checkState(typeId != null, "unknown IEarthBiomeFilter implementation: %s", this.getClass());
        return typeId;
    }

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<IEarthBiomeFilter> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.GENERATOR_SETTINGS_BIOME_FILTER);
        }
    }
}
