package net.buildtheearth.terraplusplus.dataset.osm.mapper.line;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.AbstractMapperFirst;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class LineMapperFirst extends AbstractMapperFirst<MultiLineString, LineMapper> implements LineMapper {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public LineMapperFirst(
            @JsonProperty(value = "children", required = true) @NonNull LineMapper[] children) {
        super(children);
    }
}
