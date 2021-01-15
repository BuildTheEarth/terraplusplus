package io.github.terra121.dataset.osm.config;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.osm.geojson.Geometry;
import io.github.terra121.dataset.osm.element.Element;
import lombok.NonNull;

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
    static OSMMapper<Geometry> load(@NonNull InputStream in) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(in))) {
            try {
                return GSON.fromJson(reader, Root.class);
            } catch (Exception e) {
                throw new JsonParseException(reader.toString(), e);
            }
        }
    }

    Collection<Element> apply(String id, @NonNull Map<String, String> tags, @NonNull G geometry);
}
