package io.github.terra121.dataset.geojson.feature;

import io.github.terra121.dataset.geojson.GeoJSONObject;
import io.github.terra121.dataset.geojson.Geometry;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@Data
public final class Feature implements GeoJSONObject {
    @NonNull
    protected final Geometry geometry;
    protected final Map<String, String> properties;
}
