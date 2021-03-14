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
public final class MultiPolygon implements Geometry, Iterable<Polygon> {
    @NonNull
    protected final Polygon[] polygons;

    @Override
    public Iterator<Polygon> iterator() {
        return Iterators.forArray(this.polygons);
    }

    @Override
    public MultiPolygon project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        Polygon[] out = this.polygons.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new MultiPolygon(out);
    }

    @Override
    public Bounds2d bounds() {
        return Arrays.stream(this.polygons).map(Polygon::bounds).reduce(Bounds2d::union).orElse(null);
    }
}
