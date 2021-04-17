package net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.osm.dvalue.DValue;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.polygon.DistancePolygon;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class PolygonMapperDistance implements PolygonMapper {
    protected final DrawFunction draw;
    protected final DValue layer;
    protected final int maxDist;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PolygonMapperDistance(
            @JsonProperty(value = "draw", required = true) @NonNull DrawFunction draw,
            @JsonProperty(value = "layer", required = true) @NonNull DValue layer,
            @JsonProperty("maxDist") Integer maxDist) {
        this.draw = draw;
        this.layer = layer;
        this.maxDist = fallbackIfNull(maxDist, 2);
    }

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull MultiPolygon projectedGeometry) {
        return Collections.singletonList(new DistancePolygon(id, this.layer.apply(tags), this.draw, projectedGeometry, this.maxDist));
    }
}
