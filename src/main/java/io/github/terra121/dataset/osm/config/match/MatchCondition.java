package io.github.terra121.dataset.osm.config.match;

import com.google.gson.annotations.JsonAdapter;
import io.github.terra121.dataset.vector.geojson.Geometry;
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
