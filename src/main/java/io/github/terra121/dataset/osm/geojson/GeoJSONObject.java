package io.github.terra121.dataset.osm.geojson;

import com.google.gson.annotations.JsonAdapter;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(ObjectDeserializer.class)
public interface GeoJSONObject {
}
