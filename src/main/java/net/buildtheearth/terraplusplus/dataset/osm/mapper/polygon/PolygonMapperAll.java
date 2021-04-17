package net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.AbstractMapperAll;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class PolygonMapperAll extends AbstractMapperAll<MultiPolygon, PolygonMapper> implements PolygonMapper {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PolygonMapperAll(
            @JsonProperty(value = "children", required = true) @NonNull PolygonMapper[] children) {
        super(children);
    }
}
