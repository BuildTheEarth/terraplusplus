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
public final class MultiPolygon implements Geometry, Iterable<Polygon> {
    @NonNull
    protected final Polygon[] polygons;

    @Override
    public Iterator<Polygon> iterator() {
        return Iterators.forArray(this.polygons);
    }
}
