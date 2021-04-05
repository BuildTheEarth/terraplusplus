package net.buildtheearth.terraplusplus.dataset.geojson;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.util.TerraConstants;

import java.io.Reader;

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

    /**
     * Parses a single GeoJSON geometry object from the given {@link Reader}.
     *
     * @param in the {@link Reader} to read from
     * @return the parsed GeoJSON geometry object
     */
    public static Geometry parseGeometry(@NonNull Reader in) {
        return TerraConstants.GSON.fromJson(in, Geometry.class);
    }

    /**
     * Parses a single GeoJSON geometry object from the given {@link String}.
     *
     * @param json the {@link String} containing the JSON text
     * @return the parsed GeoJSON geometry object
     */
    public static Geometry parseGeometry(@NonNull String json) {
        return TerraConstants.GSON.fromJson(json, Geometry.class);
    }
}
