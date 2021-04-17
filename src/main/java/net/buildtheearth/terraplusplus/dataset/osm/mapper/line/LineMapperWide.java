package net.buildtheearth.terraplusplus.dataset.osm.mapper.line;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.osm.dvalue.DValue;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.line.WideLine;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class LineMapperWide implements LineMapper {
    protected final DrawFunction draw;
    protected final DValue layer;
    protected final DValue radius;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public LineMapperWide(
            @JsonProperty(value = "draw", required = true) @NonNull DrawFunction draw,
            @JsonProperty(value = "layer", required = true) @NonNull DValue layer,
            @JsonProperty(value = "radius", required = true) @NonNull DValue radius) {
        this.draw = draw;
        this.layer = layer;
        this.radius = radius;
    }

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull MultiLineString projectedGeometry) {
        return Collections.singleton(new WideLine(id, this.layer.apply(tags), this.draw, projectedGeometry, this.radius.apply(tags)));
    }
}
