package net.buildtheearth.terraplusplus.generator.settings.osm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class GeneratorOSMSettingsDisable implements GeneratorOSMSettings {
    @Override
    public OSMMapper<Geometry> mapper() {
        return null; //return null to disable OpenStreetMap data entirely
    }
}
