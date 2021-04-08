package net.buildtheearth.terraplusplus.generator.settings.osm;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;

/**
 * Settings for OpenStreetMap used by {@link EarthGeneratorSettings}.
 *
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonTypeIdResolver(GeneratorOSMSettings.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface GeneratorOSMSettings {
    /**
     * @return the {@link OSMMapper} to use, or {@code null} if OpenStreetMap generation should be completely disabled
     */
    OSMMapper<Geometry> mapper();

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<GeneratorOSMSettings> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.GENERATOR_SETTINGS_OSM);
        }
    }
}
