package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Returns a non-null, empty list.
 *
 * @author DaPorkchop_
 */
public abstract class AbstractMapperNothing<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        return Collections.emptyList();
    }
}
