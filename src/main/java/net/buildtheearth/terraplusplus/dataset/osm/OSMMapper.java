package net.buildtheearth.terraplusplus.dataset.osm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;

import java.util.Collection;
import java.util.Map;

/**
 * Consumes a GeoJSON geometry object and emits some number of generateable elements.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize(as = Root.class)
@FunctionalInterface
public interface OSMMapper<G extends Geometry> {
    Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry);
}
