package io.github.terra121.dataset.geojson.geometry;

import com.google.common.collect.Iterators;
import io.github.terra121.dataset.geojson.Geometry;
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
}
