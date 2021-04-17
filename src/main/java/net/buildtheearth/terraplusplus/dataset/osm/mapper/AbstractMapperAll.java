package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Returns the combined results of all of a number of mappers, or {@code null} if any one of them returns {@code null}.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@AllArgsConstructor
public abstract class AbstractMapperAll<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @NonNull
    protected final M[] children;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        List<VectorGeometry> out = new ArrayList<>();
        int i = 0;
        for (M child : this.children) {
            Collection<VectorGeometry> result = child.apply(id + '/' + i++, tags, originalGeometry, projectedGeometry);
            if (result == null) { //don't bother processing further children
                return null;
            }
            out.addAll(result);
        }

        return out;
    }
}
