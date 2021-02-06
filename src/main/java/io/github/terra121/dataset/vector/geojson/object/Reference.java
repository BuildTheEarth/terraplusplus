package io.github.terra121.dataset.vector.geojson.object;

import io.github.terra121.dataset.vector.geojson.GeoJSONObject;
import lombok.Data;
import lombok.NonNull;

/**
 * Non-standard GeoJSON object: represents a reference to another URL containing a GeoJSON object that this object should be substituted with.
 *
 * @author DaPorkchop_
 */
@Data
public final class Reference implements GeoJSONObject {
    @NonNull
    protected final String location;
}
