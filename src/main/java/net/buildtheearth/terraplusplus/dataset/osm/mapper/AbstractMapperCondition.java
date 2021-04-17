package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchCondition;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;

import java.util.Collection;
import java.util.Map;

/**
 * Forwards elements to another mapper if a given {@link MatchCondition} matches.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@AllArgsConstructor
public abstract class AbstractMapperCondition<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @NonNull
    protected final MatchCondition match;
    @NonNull
    protected final M emit;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        if (!this.match.test(id, tags, originalGeometry, projectedGeometry)) { //element doesn't match, emit nothing
            return null;
        }

        return this.emit.apply(id, tags, originalGeometry, projectedGeometry);
    }
}
