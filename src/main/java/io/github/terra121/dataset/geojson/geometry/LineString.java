package io.github.terra121.dataset.geojson.geometry;

import io.github.terra121.dataset.geojson.Geometry;
import lombok.Data;
import lombok.NonNull;

import java.util.Objects;

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
}
