import com.fasterxml.jackson.core.JsonProcessingException;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormatTiff;
import net.minecraft.init.Bootstrap;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonSerializationTest {
    @Before
    public void initMC() {
        Bootstrap.register();
    }

    @Test
    public void test() throws JsonProcessingException {
        System.out.println(JSON_MAPPER.writeValueAsString(new TileFormatTiff(TileFormatTiff.Type.Byte, 0, null, null)));
    }

    @Test
    public void testOSMLoad() {
        OSMMapper.load();
    }

    @Test
    public void testOSMLoadSave() throws JsonProcessingException {
        OSMMapper<Geometry> mapper = OSMMapper.load();
        String json0 = JSON_MAPPER.writeValueAsString(mapper);
        System.out.println(json0);
        String json1 = JSON_MAPPER.writeValueAsString(JSON_MAPPER.readValue(json0, OSMMapper.class));
        checkState(json0.equals(json1), "inconsistent values:\n%s\n%s", json0, json1);
    }
}
