package net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.AbstractMapperCondition;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchCondition;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class PolygonMapperCondition extends AbstractMapperCondition<MultiPolygon, PolygonMapper> implements PolygonMapper {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PolygonMapperCondition(
            @JsonProperty(value = "match", required = true) @JsonAlias({ "if" }) @NonNull MatchCondition match,
            @JsonProperty(value = "emit", required = true) @JsonAlias({ "then" }) @NonNull PolygonMapper emit) {
        super(match, emit);
    }
}
