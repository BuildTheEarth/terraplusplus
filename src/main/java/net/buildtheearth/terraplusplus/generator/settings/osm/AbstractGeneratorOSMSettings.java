package net.buildtheearth.terraplusplus.generator.settings.osm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractGeneratorOSMSettings implements GeneratorOSMSettings {
    @Getter(onMethod_ = {}, lazy = true)
    private final OSMMapper<Geometry> mapper = this.initMapper();

    @SneakyThrows(IOException.class)
    protected OSMMapper<Geometry> initMapper() {
        try (InputStream in = AbstractGeneratorOSMSettings.class.getResourceAsStream("osm.json5")) {
            return uncheckedCast(JSON_MAPPER.readValue(in, OSMMapper.class));
        }
    }

    protected abstract Map<Feature, Boolean> features();

    /**
     * The different OSM features that may be toggled.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    @Getter
    public enum Feature {
        ROADS(true);

        private final boolean isDefault;
    }
}
