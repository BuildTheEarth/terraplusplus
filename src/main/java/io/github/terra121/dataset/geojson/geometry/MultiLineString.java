package io.github.terra121.dataset.geojson.geometry;

import com.google.common.collect.Iterators;
import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.projection.ProjectionFunction;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.Data;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Iterator;

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
