package io.github.terra121.dataset.geojson.geometry;

import com.google.common.collect.Iterators;
import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.projection.ProjectionFunction;
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

    @Override
    public Geometry project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        Geometry[] out = this.geometries.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new GeometryCollection(out);
    }
}
