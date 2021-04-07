package net.buildtheearth.terraplusplus.dataset.geojson.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Iterators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.ProjectionFunction;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.util.Iterator;

import static java.lang.Math.*;

/**
 * @author DaPorkchop_
 */
@Getter
@ToString
@EqualsAndHashCode
@JsonDeserialize
@JsonTypeName("MultiPoint")
public final class MultiPoint implements Geometry, Iterable<Point> {
    @Getter(onMethod_ = {
            @JsonGetter("coordinates"),
            @JsonSerialize(using = Point.ArraySerializer.class)
    })
    protected final Point[] points;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MultiPoint(
            @JsonProperty(value = "coordinates", required = true) @JsonDeserialize(using = Point.ArrayDeserializer.class) @NonNull Point[] points) {
        this.points = points;
    }

    @Override
    public Iterator<Point> iterator() {
        return Iterators.forArray(this.points);
    }

    @Override
    public MultiPoint project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        Point[] out = this.points.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new MultiPoint(out);
    }

    @Override
    public Bounds2d bounds() {
        if (this.points.length == 0) {
            return null;
        }

        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        for (Point point : this.points) {
            minLon = min(minLon, point.lon);
            maxLon = max(maxLon, point.lon);
            minLat = min(minLat, point.lat);
            maxLat = max(maxLat, point.lat);
        }
        return Bounds2d.of(minLon, maxLon, minLat, maxLat);
    }
}
