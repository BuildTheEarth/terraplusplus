package io.github.terra121.dataset.geojson.geometry;

import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.projection.ProjectionFunction;
import lombok.Data;
import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
@Data
public final class Point implements Geometry {
    protected final double lon;
    protected final double lat;

    @Override
    public Point project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        double[] proj = projection.project(this.lon, this.lat);
        return new Point(proj[0], proj[1]);
    }
}
