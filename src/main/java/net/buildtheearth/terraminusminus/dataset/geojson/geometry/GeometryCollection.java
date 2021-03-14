package net.buildtheearth.terraminusminus.dataset.geojson.geometry;

import java.util.Arrays;
import java.util.Iterator;

import com.google.common.collect.Iterators;

import lombok.Data;
import lombok.NonNull;
import net.buildtheearth.terraminusminus.dataset.geojson.Geometry;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.projection.ProjectionFunction;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

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

    @Override
    public Bounds2d bounds() {
        return Arrays.stream(this.geometries).map(Geometry::bounds).reduce(Bounds2d::union).orElse(null);
    }
}
