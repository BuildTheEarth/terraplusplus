import com.fasterxml.jackson.core.JsonProcessingException;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormatTiff;
import org.junit.Test;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
public class JsonSerializationTest {
    @Test
    public void test() throws JsonProcessingException {
        System.out.println(JSON_MAPPER.writeValueAsString(new TileFormatTiff(TileFormatTiff.Type.Byte, 0, null, null)));
    }
}
