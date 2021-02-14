package net.buildtheearth.terraplusplus.dataset.osm;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraplusplus.util.http.Disk;
import net.daporkchop.lib.binary.oio.reader.UTF8FileReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

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
