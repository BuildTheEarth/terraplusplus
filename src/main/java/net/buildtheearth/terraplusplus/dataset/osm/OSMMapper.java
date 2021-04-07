package net.buildtheearth.terraplusplus.dataset.osm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraplusplus.util.http.Disk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * Consumes a GeoJSON geometry object and emits some number of generateable elements.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize(as = Root.class)
@FunctionalInterface
public interface OSMMapper<G extends Geometry> {
    @SneakyThrows(IOException.class)
    static OSMMapper<Geometry> load() {
        Path path = Disk.configFile("osm.json5");
        try (InputStream in = Files.exists(path) ? Files.newInputStream(path) : OSMMapper.class.getResourceAsStream("osm.json5")) {
            return uncheckedCast(JSON_MAPPER.readValue(in, OSMMapper.class));
        }
    }

    Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry);
}
