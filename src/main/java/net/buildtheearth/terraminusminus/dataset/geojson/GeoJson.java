package net.buildtheearth.terraminusminus.dataset.geojson;

import java.io.Reader;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraminusminus.TerraConstants;

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
        return TerraConstants.GSON.fromJson(in, GeoJsonObject.class);
    }

    /**
     * Parses a single GeoJSON object from the given {@link String}.
     *
     * @param json the {@link String} containing the JSON text
     * @return the parsed GeoJSON object
     */
    public static GeoJsonObject parse(@NonNull String json) {
        return TerraConstants.GSON.fromJson(json, GeoJsonObject.class);
    }
}
