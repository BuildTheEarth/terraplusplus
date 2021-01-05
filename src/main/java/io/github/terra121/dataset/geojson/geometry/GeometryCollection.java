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
public final class GeometryCollection implements Geometry, Iterable<Geometry> {
    @NonNull
    protected final Geometry[] geometries;

    @Override
    public Iterator<Geometry> iterator() {
        return Iterators.forArray(this.geometries);
    }
}
