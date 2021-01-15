package io.github.terra121.dataset.osm.geojson.geometry;

import com.google.common.collect.Iterators;
import io.github.terra121.dataset.osm.geojson.Geometry;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.projection.ProjectionFunction;
import lombok.Data;
import lombok.NonNull;

import java.util.Iterator;

/**
 * @author DaPorkchop_
 */
@Data
public final class MultiPoint implements Geometry, Iterable<Point> {
    @NonNull
    protected final Point[] points;

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
}
