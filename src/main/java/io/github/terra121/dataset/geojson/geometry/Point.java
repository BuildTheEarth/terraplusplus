package io.github.terra121.dataset.geojson.geometry;

import io.github.terra121.dataset.geojson.Geometry;
import lombok.Data;

/**
 * @author DaPorkchop_
 */
@Data
public final class Point implements Geometry {
    protected final double lon;
    protected final double lat;
}
