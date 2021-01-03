package io.github.terra121.dataset.geojson.object;

import io.github.terra121.dataset.geojson.Geometry;
import lombok.Data;
import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
@Data
public final class FeatureCollection implements Geometry {
    @NonNull
    protected final Feature[] features;
}
