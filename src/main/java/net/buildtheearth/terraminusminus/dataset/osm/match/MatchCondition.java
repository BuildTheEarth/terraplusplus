package net.buildtheearth.terraminusminus.dataset.osm.match;

import com.google.gson.annotations.JsonAdapter;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.dataset.geojson.Geometry;

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
