package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;

import java.util.Collection;
import java.util.Map;

/**
 * Returns the result of the first of a number of mappers that returned a non-{@code null} value.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@AllArgsConstructor
public abstract class AbstractMapperFirst<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @NonNull
    protected final M[] children;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        for (M child : this.children) {
            Collection<VectorGeometry> result = child.apply(id, tags, originalGeometry, projectedGeometry);
            if (result != null) {
                return result;
            }
        }

        return null; //none matched!
    }
}
