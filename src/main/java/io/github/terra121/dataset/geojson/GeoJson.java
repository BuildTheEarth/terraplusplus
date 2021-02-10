package io.github.terra121.dataset.geojson;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.Reader;

import static io.github.terra121.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class GeoJson {
    /**
     * Parses a single GeoJSON object from the given {@link Reader}.
     *
     * @param in the {@link Reader} to read from
     * @return the parsed GeoJSON object
     */
    public static GeoJsonObject parse(@NonNull Reader in) {
        return GSON.fromJson(in, GeoJsonObject.class);
    }

    /**
     * Parses a single GeoJSON object from the given {@link String}.
     *
     * @param json the {@link String} containing the JSON text
     * @return the parsed GeoJSON object
     */
    public static GeoJsonObject parse(@NonNull String json) {
        return GSON.fromJson(json, GeoJsonObject.class);
    }
}
