package io.github.terra121.dataset.geojson.object;

import io.github.terra121.dataset.geojson.GeoJsonObject;
import lombok.Data;
import lombok.NonNull;

/**
 * Non-standard GeoJSON object: represents a reference to another URL containing a GeoJSON object that this object should be substituted with.
 *
 * @author DaPorkchop_
 */
@Data
public final class Reference implements GeoJsonObject {
    @NonNull
    protected final String location;
}
