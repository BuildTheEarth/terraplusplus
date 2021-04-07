package net.buildtheearth.terraplusplus.dataset.osm.mapper.line;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.AbstractMapperCondition;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchCondition;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class LineMapperCondition extends AbstractMapperCondition<MultiLineString, LineMapper> implements LineMapper {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public LineMapperCondition(
            @JsonProperty(value = "match", required = true) @JsonAlias({ "if" }) @NonNull MatchCondition match,
            @JsonProperty(value = "emit", required = true) @JsonAlias({ "then" }) @NonNull LineMapper emit) {
        super(match, emit);
    }
}
