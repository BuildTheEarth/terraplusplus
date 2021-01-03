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
public final class MultiLineString implements Geometry, Iterable<LineString> {
    @NonNull
    protected final LineString[] lines;

    @Override
    public Iterator<LineString> iterator() {
        return Iterators.forArray(this.lines);
    }
}
