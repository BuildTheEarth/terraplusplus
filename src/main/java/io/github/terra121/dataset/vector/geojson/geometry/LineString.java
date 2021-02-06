package io.github.terra121.dataset.vector.geojson.geometry;

import io.github.terra121.dataset.vector.geojson.Geometry;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.projection.ProjectionFunction;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.Data;
import lombok.NonNull;

import java.util.Objects;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Data
public final class LineString implements Geometry {
    protected final Point[] points;

    public LineString(@NonNull Point[] points) {
        checkArg(points.length >= 2, "LineString must contain at least 2 points!");
        this.points = points;
    }

    public boolean isLinearRing() {
        return this.points.length >= 4 && Objects.equals(this.points[0], this.points[this.points.length - 1]);
    }

    @Override
    public LineString project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        Point[] out = this.points.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new LineString(out);
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
