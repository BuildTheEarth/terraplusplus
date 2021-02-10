package net.buildtheearth.terraplusplus.dataset.osm.match;

import com.google.gson.annotations.JsonAdapter;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import lombok.NonNull;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(MatchParser.class)
@FunctionalInterface
public interface MatchCondition {
    /**
     * Always returns {@code false}.
     */
    MatchCondition FALSE = (id, tags, originalGeometry, projectedGeometry) -> false;

    boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry);
}
