import com.fasterxml.jackson.core.JsonProcessingException;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormatTiff;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileTransform;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonSerializationTest {
    @Test
    public void test() throws JsonProcessingException {
        System.out.println(JSON_MAPPER.writeValueAsString(new TileFormatTiff(TileFormatTiff.Type.Byte, 0, null, null, TileTransform.NONE)));
    }
}
