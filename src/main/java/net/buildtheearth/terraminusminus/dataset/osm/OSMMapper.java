package net.buildtheearth.terraminusminus.dataset.osm;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraminusminus.TerraConstants;
import net.buildtheearth.terraminusminus.dataset.geojson.Geometry;
import net.buildtheearth.terraminusminus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraminusminus.util.http.Disk;
import net.daporkchop.lib.binary.oio.reader.UTF8FileReader;

/**
 * Consumes a GeoJSON geometry object and emits some number of generateable elements.
 *
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface OSMMapper<G extends Geometry> {
    @SneakyThrows(IOException.class)
    static OSMMapper<Geometry> load() {
        Path path = Disk.configFile("osm.json5");
        try (JsonReader reader = new JsonReader(Files.exists(path)
                ? new UTF8FileReader(path.toString())
                : new InputStreamReader(OSMMapper.class.getResourceAsStream("osm.json5")))) {
            try {
                return TerraConstants.GSON.fromJson(reader, Root.class);
            } catch (Exception e) {
                throw new JsonParseException(reader.toString(), e);
            }
        }
    }

    Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry);
}
