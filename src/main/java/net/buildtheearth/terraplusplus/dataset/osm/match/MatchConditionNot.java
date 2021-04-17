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
 * Inverts the result of a single match condition.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class MatchConditionNot implements MatchCondition {
    protected final MatchCondition child;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MatchConditionNot(
            @JsonProperty(value = "child", required = true) @NonNull MatchCondition child) {
        this.child = child;
    }

    @Override
    public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        return !this.child.test(id, tags, originalGeometry, projectedGeometry);
    }
}
