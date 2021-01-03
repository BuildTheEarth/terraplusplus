package io.github.terra121.dataset.geojson.geometry;

import io.github.terra121.dataset.geojson.Geometry;
import lombok.Data;
import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
@Data
public final class MultiPoint implements Geometry {
    @NonNull
    protected final Point[] points;
}
