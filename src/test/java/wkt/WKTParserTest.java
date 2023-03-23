package wkt;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParser;
import net.buildtheearth.terraplusplus.projection.wkt.WKTStyle;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.Objects;
import java.util.Properties;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static org.junit.Assert.*;

/**
 * @author DaPorkchop_
 */
public class WKTParserTest {
    private static CharBuffer buffer(@NonNull String text) {
        return CharBuffer.wrap(text.toCharArray()).asReadOnlyBuffer();
    }

    private static final Properties EPSG = new Properties();

    @BeforeClass
    public static void loadProperties() throws IOException {
        try (InputStream in = new BufferedInputStream(Objects.requireNonNull(WKTParserTest.class.getResourceAsStream("epsg.properties")))) {
            EPSG.load(in);
        }
    }

    @Test
    public void testWKTFormat() {
        EPSG.forEach((key, wkt) -> {
            String formatted = WKTStyle.ONE_LINE.format(wkt.toString());
            assertEquals(wkt.toString(), formatted);
        });
    }

    @Test
    public void testWKTParse() {
        EPSG.forEach((key, wkt) -> {
            WKTObject parsed = WKTParser.parse(buffer(wkt.toString()));
            String formatted = parsed.toString(WKTStyle.ONE_LINE);
            assertEquals(wkt.toString(), formatted);
        });
    }

    @Test
    public void testEllipsoid() throws JsonProcessingException {
        System.out.println(JSON_MAPPER.readValue(
                "{\"$schema\": \"https://proj.org/schemas/v0.5/projjson.schema.json\",\"type\": \"Ellipsoid\",\"name\": \"WGS 84\",\"semi_major_axis\": 6378137,\"inverse_flattening\": 298.257223563,\"id\": {\"authority\": \"EPSG\",\"code\": 7030}}",
                WKTObject.AutoDeserialize.class).asWKTObject().toPrettyString());
    }

    @Test
    public void testDatum() throws JsonProcessingException {
        System.out.println(JSON_MAPPER.readValue(
                "{\"$schema\": \"https://proj.org/schemas/v0.5/projjson.schema.json\",\"type\": \"DynamicGeodeticReferenceFrame\",\"name\": \"IGS97\",\"frame_reference_epoch\": 1997,\"ellipsoid\": {\"name\": \"GRS 1980\",\"semi_major_axis\": 6378137,\"inverse_flattening\": 298.257222101},\"scope\": \"Geodesy.\",\"area\": \"World.\",\"bbox\": {\"south_latitude\": -90,\"west_longitude\": -180,\"north_latitude\": 90,\"east_longitude\": 180},\"id\": {\"authority\": \"EPSG\",\"code\": 1244}}",
                WKTObject.AutoDeserialize.class).asWKTObject().toPrettyString());

        System.out.println(JSON_MAPPER.readValue(
                "{\"$schema\": \"https://proj.org/schemas/v0.5/projjson.schema.json\",\"type\": \"DatumEnsemble\",\"name\": \"World Geodetic System 1984 ensemble\",\"members\": [{\"name\": \"World Geodetic System 1984 (Transit)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1166}},{\"name\": \"World Geodetic System 1984 (G730)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1152}},{\"name\": \"World Geodetic System 1984 (G873)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1153}},{\"name\": \"World Geodetic System 1984 (G1150)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1154}},{\"name\": \"World Geodetic System 1984 (G1674)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1155}},{\"name\": \"World Geodetic System 1984 (G1762)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1156}},{\"name\": \"World Geodetic System 1984 (G2139)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1309}}],\"ellipsoid\": {\"name\": \"WGS 84\",\"semi_major_axis\": 6378137,\"inverse_flattening\": 298.257223563},\"accuracy\": \"2.0\",\"id\": {\"authority\": \"EPSG\",\"code\": 6326}}",
                WKTObject.AutoDeserialize.class).asWKTObject().toPrettyString());
    }

    @Test
    public void testPrimeMeridian() {
    }

    @Test
    public void testGeographicCRS() {
    }
}
