package net.buildtheearth.terraplusplus.generator.settings.biome;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.generator.biome.IEarthBiomeFilter;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Settings for biome generation used by {@link EarthGeneratorSettings}.
 *
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonTypeIdResolver(GeneratorBiomeSettings.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface GeneratorBiomeSettings {
    /**
     * @return the {@link IEarthBiomeFilter} to use
     */
    IEarthBiomeFilter<?> filter();

    /**
     * @return this {@link IEarthBiomeFilter}'s type ID
     */
    default String typeId() {
        String typeId = GlobalParseRegistries.GENERATOR_SETTINGS_BIOME.inverse().get(this.getClass());
        checkState(typeId != null, "unknown GeneratorBiomeSettings implementation: %s", this.getClass());
        return typeId;
    }

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<GeneratorBiomeSettings> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.GENERATOR_SETTINGS_BIOME);
        }
    }
}
