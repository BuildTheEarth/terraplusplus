package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTStaticGeographicCRS;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTPrimeMeridian;

import java.io.IOException;
import java.nio.CharBuffer;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class WKTParser {
    @SneakyThrows(IOException.class)
    public static WKTEllipsoid parseEllipsoid(@NonNull CharBuffer buffer) {
        try (WKTReader reader = new WKTReader.FromCharBuffer(buffer)) {
            return WKTEllipsoid.PARSE_SCHEMA.parse(reader);
        }
    }

    @SneakyThrows(IOException.class)
    public static WKTDatum parseDatum(@NonNull CharBuffer buffer) {
        try (WKTReader reader = new WKTReader.FromCharBuffer(buffer)) {
            return WKTDatum.PARSE_SCHEMA.parse(reader);
        }
    }

    @SneakyThrows(IOException.class)
    public static WKTPrimeMeridian parsePrimeMeridian(@NonNull CharBuffer buffer) {
        try (WKTReader reader = new WKTReader.FromCharBuffer(buffer)) {
            return WKTPrimeMeridian.PARSE_SCHEMA.parse(reader);
        }
    }

    @SneakyThrows(IOException.class)
    public static WKTStaticGeographicCRS parseStaticGeographicCRS(@NonNull CharBuffer buffer) {
        try (WKTReader reader = new WKTReader.FromCharBuffer(buffer)) {
            return WKTStaticGeographicCRS.PARSE_SCHEMA.parse(reader);
        }
    }
}
