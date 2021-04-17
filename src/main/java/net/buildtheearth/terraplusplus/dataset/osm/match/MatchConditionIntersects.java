package net.buildtheearth.terraplusplus.dataset.osm.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class MatchConditionIntersects implements MatchCondition {
    @Getter(onMethod_ = { @JsonGetter })
    protected final GeographicProjection projection;
    @Getter(onMethod_ = { @JsonGetter })
    protected final Geometry geometry;

    protected transient final Bounds2d bb;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    @SneakyThrows(OutOfProjectionBoundsException.class)
    public MatchConditionIntersects(
            @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection,
            @JsonProperty(value = "geometry", required = true) @NonNull Geometry geometry) {
        this.projection = projection;
        this.geometry = geometry;
        this.bb = geometry.project(projection::toGeo).bounds();
    }

    @Override
    public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        Bounds2d bounds = originalGeometry.bounds();
        return bounds != null && this.bb.intersects(bounds);
    }
}
