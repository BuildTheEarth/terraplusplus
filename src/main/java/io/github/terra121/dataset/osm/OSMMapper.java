package io.github.terra121.dataset.osm;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.dataset.geojson.Geometry;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

import static io.github.terra121.TerraConstants.*;

/**
 * Consumes a GeoJSON geometry object and emits some number of generateable elements.
 *
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface OSMMapper<G extends Geometry> {
    @SneakyThrows(IOException.class)
    static OSMMapper<Geometry> load() {
        try (JsonReader reader = new JsonReader(new InputStreamReader(OSMMapper.class.getResourceAsStream("osm.json5")))) {
            try {
                return GSON.fromJson(reader, Root.class);
            } catch (Exception e) {
                throw new JsonParseException(reader.toString(), e);
            }
        }
    }

    Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry);
}
