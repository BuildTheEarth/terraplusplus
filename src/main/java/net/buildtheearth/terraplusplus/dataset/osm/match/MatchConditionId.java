package net.buildtheearth.terraplusplus.dataset.osm.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = {@JsonGetter })
@JsonDeserialize
public final class MatchConditionId implements MatchCondition {
    protected final Set<String> ids;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MatchConditionId(
            @JsonProperty(value = "ids", required = true) @NonNull String[] ids) {
        this.ids = ImmutableSet.copyOf(Stream.of(ids).map(String::intern).toArray(String[]::new));
    }

    @Override
    public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        return this.ids.contains(id);
    }
}
