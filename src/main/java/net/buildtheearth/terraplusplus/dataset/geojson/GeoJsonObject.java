package net.buildtheearth.terraplusplus.dataset.geojson;

import com.google.gson.annotations.JsonAdapter;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(ObjectDeserializer.class)
public interface GeoJsonObject {
}
