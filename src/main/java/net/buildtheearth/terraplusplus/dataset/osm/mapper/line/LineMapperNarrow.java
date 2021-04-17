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
import net.buildtheearth.terraplusplus.dataset.vector.geometry.line.NarrowLine;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class LineMapperNarrow implements LineMapper {
    protected final DrawFunction draw;
    protected final DValue layer;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public LineMapperNarrow(
            @JsonProperty(value = "draw", required = true) @NonNull DrawFunction draw,
            @JsonProperty(value = "layer", required = true) @NonNull DValue layer) {
        this.draw = draw;
        this.layer = layer;
    }

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull MultiLineString projectedGeometry) {
        return Collections.singletonList(new NarrowLine(id, this.layer.apply(tags), this.draw, projectedGeometry, 1));
    }
}
