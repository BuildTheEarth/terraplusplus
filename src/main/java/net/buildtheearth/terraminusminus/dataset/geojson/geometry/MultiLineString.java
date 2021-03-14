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
public final class MultiLineString implements Geometry, Iterable<LineString> {
    @NonNull
    protected final LineString[] lines;

    @Override
    public Iterator<LineString> iterator() {
        return Iterators.forArray(this.lines);
    }

    @Override
    public MultiLineString project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        LineString[] out = this.lines.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new MultiLineString(out);
    }

    @Override
    public Bounds2d bounds() {
        return Arrays.stream(this.lines).map(LineString::bounds).reduce(Bounds2d::union).orElse(null);
    }
}
