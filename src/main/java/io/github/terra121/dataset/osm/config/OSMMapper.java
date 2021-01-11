package io.github.terra121.dataset.osm.config;

import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.dataset.osm.Generatable;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

import static io.github.terra121.TerraConstants.*;

/**
 * Consumes a GeoJSON geometry object and emits some number of generateable elements.
 *
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface OSMMapper<G extends Geometry> {
    static OSMMapper<Geometry> load(@NonNull InputStream in) throws IOException {
        return GSON.fromJson(new InputStreamReader(in), Root.class);
    }

    Collection<Generatable> apply(String id, @NonNull Map<String, String> tags, @NonNull G geometry);
}
