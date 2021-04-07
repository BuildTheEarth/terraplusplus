package geojson;

import net.buildtheearth.terraplusplus.dataset.geojson.GeoJsonObject;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public class GeoJsonTest {
    @Test
    public void test0() throws IOException {
        System.out.println(JSON_MAPPER.readValue("{\"type\":\"LineString\",\"coordinates\":[[1,3],[3.5,25,6]]}", GeoJsonObject.class));
        System.out.println(JSON_MAPPER.readValue("{\"type\":\"LineString\",\"coordinates\":[[1,3],[3.5,25,6]]}", Geometry.class));
    }

    @Test
    public void test1() throws IOException {
        for (int i = 0; i <= 2; i++) {
            try (InputStream in = GeoJsonTest.class.getResourceAsStream(i + ".json")) {
                GeoJsonObject o = JSON_MAPPER.readValue(in, GeoJsonObject.class);
                System.out.println(o);
                String json0 = JSON_MAPPER.writeValueAsString(o);
                System.out.println(json0);
                String json1 = JSON_MAPPER.writeValueAsString(JSON_MAPPER.readValue(json0, GeoJsonObject.class));
                checkState(json0.equals(json1), "inconsistent values:\n%s\n%s", json0, json1);
            }
        }
    }
}
