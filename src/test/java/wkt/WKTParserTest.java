package wkt;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParser;
import org.junit.Test;

import java.nio.CharBuffer;

import static org.junit.Assert.*;

/**
 * @author DaPorkchop_
 */
public class WKTParserTest {
    private static CharBuffer buffer(@NonNull String text) {
        return CharBuffer.wrap(text.toCharArray()).asReadOnlyBuffer();
    }

    @Test
    public void testEllipsoid() {
        assertEquals(
                "ELLIPSOID[\"WGS 84\", 6378137.0, 298.257223563, LENGTHUNIT[\"metre\", 1.0], ID[\"EPSG\", 7030]]",
                WKTParser.parseEllipsoid(buffer("ELLIPSOID[\"WGS 84\",6378137,298.257223563,LENGTHUNIT[\"metre\",1],ID[\"EPSG\",7030]]")).toString());

        assertEquals(
                "ELLIPSOID[\"GRS 1980\", 6378137.0, 298.257222101, LENGTHUNIT[\"metre\", 1.0]]",
                WKTParser.parseEllipsoid(buffer("SPHEROID[\"GRS 1980\",6378137.0,298.257222101]")).toString());

        assertEquals(
                "ELLIPSOID[\"Clark 1866\", 2.0925832164E7, 294.97869821, LENGTHUNIT[\"US survey foot\", 0.304800609601219]]",
                WKTParser.parseEllipsoid(buffer("ELLIPSOID[\"Clark 1866\",20925832.164,294.97869821, LENGTHUNIT[\"US survey foot\",0.304800609601219]]")).toString());

        assertEquals(
                "ELLIPSOID[\"Clark 1866\", 2.0925832164E7, 294.97869821, LENGTHUNIT[\"US survey foot\", 0.304800609601219]]",
                WKTParser.parseEllipsoid(buffer("ELLIPSOID[\"Clark 1866\",2.0925832164E7,294.97869821, LENGTHUNIT[\"US survey foot\",0.304800609601219]]")).toString());

        assertEquals(
                "ELLIPSOID[\"Sphere\", 6371000.0, 0.0, LENGTHUNIT[\"metre\", 1.0]]",
                WKTParser.parseEllipsoid(buffer("ELLIPSOID[\"Sphere\",6371000,0,LENGTHUNIT[\"metre\",1.0]]")).toString());
    }

    @Test
    public void testDatum() {
        assertEquals(
                "DATUM[\"North American Datum 1983\", ELLIPSOID[\"GRS 1980\", 6378137.0, 298.257222101, LENGTHUNIT[\"metre\", 1.0]]]",
                WKTParser.parseDatum(buffer("DATUM[\"North American Datum 1983\", ELLIPSOID[\"GRS 1980\", 6378137, 298.257222101, LENGTHUNIT[\"metre\", 1.0]]]")).toString());

        assertEquals(
                "DATUM[\"World Geodetic System 1984\", ELLIPSOID[\"WGS 84\", 6378388.0, 298.257223563, LENGTHUNIT[\"metre\", 1.0]]]",
                WKTParser.parseDatum(buffer("TRF[\"World Geodetic System 1984\", ELLIPSOID[\"WGS 84\",6378388.0,298.257223563,LENGTHUNIT[\"metre\",1.0]]]")).toString());

        assertEquals(
                "DATUM[\"Tananarive 1925\", ELLIPSOID[\"International 1924\", 6378388.0, 297.0, LENGTHUNIT[\"metre\", 1.0]], ANCHOR[\"Tananarive observatory:21.0191667gS, 50.23849537gE of Paris\"]]",
                WKTParser.parseDatum(buffer("GEODETICDATUM[\"Tananarive 1925\", ELLIPSOID[\"International 1924\",6378388.0,297.0,LENGTHUNIT[\"metre\",1.0] ], ANCHOR[\"Tananarive observatory:21.0191667gS, 50.23849537gE of Paris\"]]")).toString());
    }

    @Test
    public void testPrimeMeridian() {
        assertEquals(
                "PRIMEM[\"Paris\", 2.5969213, ANGLEUNIT[\"grad\", 0.015707963267949]]",
                WKTParser.parsePrimeMeridian(buffer("PRIMEM[\"Paris\",2.5969213,ANGLEUNIT[\"grad\",0.015707963267949]]")).toString());
        assertEquals(
                "PRIMEM[\"Ferro\", -17.6666667]",
                WKTParser.parsePrimeMeridian(buffer("PRIMEM[\"Ferro\",-17.6666667]")).toString());
        assertEquals(
                "PRIMEM[\"Greenwich\", 0.0, ANGLEUNIT[\"degree\", 0.0174532925199433]]",
                WKTParser.parsePrimeMeridian(buffer("PRIMEM[\"Greenwich\",0.0, ANGLEUNIT[\"degree\",0.0174532925199433]]")).toString());
    }

    @Test
    public void testStaticGeographicCRS() {
        assertEquals(
                "GEOGCS[\"NAD83\", DATUM[\"North American Datum 1983\", ELLIPSOID[\"GRS 1980\", 6378137.0, 298.257222101, LENGTHUNIT[\"metre\", 1.0]]], PRIMEM[\"Greenwich\", 0.0], ANGLEUNIT[\"degree\", 0.0174532925199433]]",
                WKTParser.parseStaticGeographicCRS(buffer("GEOGCS[\"NAD83\", DATUM[\"North American Datum 1983\", ELLIPSOID[\"GRS 1980\", 6378137.0, 298.257222101]], PRIMEM[\"Greenwich\",0], UNIT[\"degree\", 0.0174532925199433]]")).toString());

        /*assertEquals(
                "GEOGCS[\"NAD83\", DATUM[\"North American Datum 1983\", SPHEROID[\"GRS 1980\", 6378137.0, 298.257222101]], PRIMEM[\"Greenwich\", 0.0], AXIS[\"latitude\", NORTH], AXIS[\"longitude\", EAST], ANGLEUNIT[\"degree\", 0.0174532925199433]]",
                WKTParser.parseStaticGeographicCRS(buffer("GEOGCS[\"NAD83\", DATUM[\"North American Datum 1983\", SPHEROID[\"GRS 1980\", 6378137.0, 298.257222101]], PRIMEM[\"Greenwich\", 0], AXIS[\"latitude\",NORTH], AXIS[\"longitude\",EAST], UNIT[\"degree\",0.0174532925199433]]")).toString());*/
    }
}
