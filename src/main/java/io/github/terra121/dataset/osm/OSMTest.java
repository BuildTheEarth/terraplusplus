package io.github.terra121.dataset.osm;

import io.github.terra121.dataset.osm.config.OSMMapper;
import io.github.terra121.dataset.osm.segment.OSMSegment;
import io.github.terra121.generator.EarthGeneratorSettings;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.bvh.Bounds2d;
import net.daporkchop.lib.binary.oio.appendable.PAppendable;
import net.daporkchop.lib.binary.oio.writer.UTF8FileWriter;
import net.daporkchop.lib.common.function.io.IOConsumer;
import net.daporkchop.lib.common.misc.file.PFiles;
import net.daporkchop.lib.common.misc.string.PStrings;
import net.minecraft.init.Bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author DaPorkchop_
 */
public class OSMTest {
    public static void main(String... args) throws OutOfProjectionBoundsException, IOException {
        Bootstrap.register();

        OSMMapper config;
        try (InputStream in = OSMTest.class.getResourceAsStream("/osm_config_default.json5")) {
            config = OSMMapper.load(in);
        }

        GeographicProjection projection = new EarthGeneratorSettings("").getProjection();
        Bounds2d bounds2d = Bounds2d.of(8.57707d, 8.57707d, 47.21767d, 47.21767d).expand(0.001d);
        Bounds2d intersectionBounds = bounds2d.toCornerBB(projection, true).fromGeo().axisAlign();

        new OpenStreetMap(projection, true, true, true)
                .getRegionsAsync(bounds2d.toCornerBB(projection, true))
                .thenAccept((IOConsumer<OSMRegion[]>) regions -> {
                    try (PAppendable out = new UTF8FileWriter(PFiles.ensureFileExists(new File("/home/daporkchop/Desktop/tpp.json")))) {
                        out.append("{\"type\":\"FeatureCollection\",\"features\":[");
                        AtomicBoolean started = new AtomicBoolean(false);
                        for (OSMRegion region : regions) {
                            region.segments.forEach((IOConsumer<OSMSegment>) s -> {
                                if (!started.compareAndSet(false, true)) {
                                    out.append(',');
                                }
                                out.append(PStrings.fastFormat("{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[%s,%s],[%s,%s]]},\"properties\":{\"type\":\"%s\"}}",
                                        s.lon0, s.lat0, s.lon1, s.lat1, s.type));
                            });
                        }
                        out.appendLn("]}");
                    }
                })
                .join();
    }
}
