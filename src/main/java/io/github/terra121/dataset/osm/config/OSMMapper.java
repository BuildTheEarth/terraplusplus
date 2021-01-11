package io.github.terra121.dataset.osm.config;

import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.dataset.osm.Generatable;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;

/**
 * Consumes a GeoJSON geometry object and emits some number of generateable elements.
 *
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface OSMMapper<G extends Geometry> {
    Collection<Generatable> apply(String id, @NonNull Map<String, String> tags, @NonNull G geometry);
}
