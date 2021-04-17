package net.buildtheearth.terraplusplus.dataset.osm.match;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
@JsonTypeIdResolver(MatchCondition.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface MatchCondition {
    /**
     * Always returns {@code false}.
     */
    MatchCondition FALSE = (id, tags, originalGeometry, projectedGeometry) -> false;

    boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry);

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<MatchCondition> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.OSM_MATCH_CONDITIONS);
        }
    }
}
