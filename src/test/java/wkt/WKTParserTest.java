package wkt;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParser;
import org.junit.Test;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * @author DaPorkchop_
 */
public class WKTParserTest {
    private static PushbackReader reader(@NonNull String text) {
        return new PushbackReader(new StringReader(text));
    }

    @Test
    @SneakyThrows(IOException.class)
    public void testEllipsoid() {
        assertEquals(
                "ELLIPSOID[\"WGS 84\", 6378137.0, 298.257223563, LENGTHUNIT[\"metre\", 1.0], ID[\"EPSG\", 7030]]",
                WKTParser.parseEllipsoid(reader("ELLIPSOID[\"WGS 84\",6378137,298.257223563,LENGTHUNIT[\"metre\",1],ID[\"EPSG\",7030]]")).toString());

        assertEquals(
                "ELLIPSOID[\"GRS 1980\", 6378137.0, 298.257222101, LENGTHUNIT[\"metre\", 1.0]]",
                WKTParser.parseEllipsoid(reader("SPHEROID[\"GRS 1980\",6378137.0,298.257222101]")).toString());

        assertEquals(
                "ELLIPSOID[\"Clark 1866\", 2.0925832164E7, 294.97869821, LENGTHUNIT[\"US survey foot\", 0.3048006096012191]]",
                WKTParser.parseEllipsoid(reader("ELLIPSOID[\"Clark 1866\",20925832.164,294.97869821,\n"
                                                + "    LENGTHUNIT[\"US survey foot\",0.304800609601219]]")).toString());

        assertEquals(
                "ELLIPSOID[\"Clark 1866\", 2.0925832164E7, 294.97869821, LENGTHUNIT[\"US survey foot\", 0.3048006096012191]]",
                WKTParser.parseEllipsoid(reader("ELLIPSOID[\"Clark 1866\",2.0925832164E7,294.97869821,\n"
                                                + "    LENGTHUNIT[\"US survey foot\",0.304800609601219]]")).toString());

        assertEquals(
                "ELLIPSOID[\"Sphere\", 6371000.0, 0.0, LENGTHUNIT[\"metre\", 1.0]]",
                WKTParser.parseEllipsoid(reader("ELLIPSOID[\"Sphere\",6371000,0,LENGTHUNIT[\"metre\",1.0]]")).toString());
    }
}
