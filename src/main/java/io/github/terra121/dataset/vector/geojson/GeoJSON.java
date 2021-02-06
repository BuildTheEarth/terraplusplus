package io.github.terra121.dataset.vector.geojson;

import com.google.gson.Gson;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.Reader;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class GeoJSON {
    private static final Gson GSON = new Gson();

    /**
     * Parses a single GeoJSON object from the given {@link Reader}.
     *
     * @param in the {@link Reader} to read from
     * @return the parsed GeoJSON object
     */
    public static GeoJSONObject parse(@NonNull Reader in) {
        return GSON.fromJson(in, GeoJSONObject.class);
    }

    /**
     * Parses a single GeoJSON object from the given {@link String}.
     *
     * @param json the {@link String} containing the JSON text
     * @return the parsed GeoJSON object
     */
    public static GeoJSONObject parse(@NonNull String json) {
        return GSON.fromJson(json, GeoJSONObject.class);
    }
}
