package net.buildtheearth.terraplusplus.dataset.osm.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;

import java.util.Map;

/**
 * Combines the results of multiple match conditions using a logical OR operation.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class MatchConditionOr implements MatchCondition {
    protected final MatchCondition[] children;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MatchConditionOr(
            @JsonProperty(value = "children", required = true) @NonNull MatchCondition[] children) {
        this.children = children;
    }

    @Override
    public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        for (MatchCondition delegate : this.children) {
            if (delegate.test(id, tags, originalGeometry, projectedGeometry)) {
                return true;
            }
        }
        return false;
    }
}
