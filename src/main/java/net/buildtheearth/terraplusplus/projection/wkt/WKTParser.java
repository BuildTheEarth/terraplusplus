package net.buildtheearth.terraplusplus.projection.wkt;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.projection.wkt.crs.AbstractWKTCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTAxis;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTCompdCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTEngineeringCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTGeocentricCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTGeographicCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTProjectedCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTVertCRS;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTEngineeringDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTVertDatum;
import net.buildtheearth.terraplusplus.projection.wkt.projection.WKTProjection;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDatumAnchor;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTPrimeMeridian;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTTOWGS84;
import net.buildtheearth.terraplusplus.projection.wkt.projection.WKTProjectionParameter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTScaleUnit;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class WKTParser {
    private static final List<WKTParseSchema<? extends WKTObject>> ALL_SCHEMAS = Arrays.asList(
            WKTAngleUnit.PARSE_SCHEMA,
            WKTAxis.PARSE_SCHEMA,
            WKTCompdCRS.PARSE_SCHEMA,
            WKTDatum.PARSE_SCHEMA,
            WKTDatumAnchor.PARSE_SCHEMA,
            WKTEllipsoid.PARSE_SCHEMA,
            WKTEngineeringCRS.PARSE_SCHEMA,
            WKTEngineeringDatum.PARSE_SCHEMA,
            WKTGeocentricCRS.PARSE_SCHEMA,
            WKTGeographicCRS.PARSE_SCHEMA,
            WKTID.PARSE_SCHEMA,
            WKTLengthUnit.PARSE_SCHEMA,
            WKTPrimeMeridian.PARSE_SCHEMA,
            WKTProjectedCRS.PARSE_SCHEMA,
            WKTProjection.PARSE_SCHEMA,
            WKTProjectionParameter.PARSE_SCHEMA,
            WKTScaleUnit.PARSE_SCHEMA,
            WKTTOWGS84.PARSE_SCHEMA,
            WKTVertCRS.PARSE_SCHEMA,
            WKTVertDatum.PARSE_SCHEMA
    );

    public static final ImmutableList<WKTParseSchema<? extends AbstractWKTCRS>> CRS_SCHEMAS = ImmutableList.of(
            WKTCompdCRS.PARSE_SCHEMA,
            WKTEngineeringCRS.PARSE_SCHEMA,
            WKTGeocentricCRS.PARSE_SCHEMA,
            WKTGeographicCRS.PARSE_SCHEMA,
            WKTProjectedCRS.PARSE_SCHEMA,
            WKTVertCRS.PARSE_SCHEMA
    );

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
    public static WKTGeographicCRS parseGeographicCRS(@NonNull CharBuffer buffer) {
        try (WKTReader reader = new WKTReader.FromCharBuffer(buffer)) {
            return WKTGeographicCRS.PARSE_SCHEMA.parse(reader);
        }
    }

    @SneakyThrows(IOException.class)
    public static WKTObject parse(@NonNull CharBuffer buffer) {
        try (WKTReader reader = new WKTReader.FromCharBuffer(buffer)) {
            String keyword = reader.nextKeyword();
            for (WKTParseSchema<? extends WKTObject> schema : ALL_SCHEMAS) {
                if (schema.isPermittedKeyword(keyword)) {
                    return schema.parse(reader, keyword);
                }
            }
            throw new IllegalArgumentException("unknown keyword: " + keyword);
        }
    }
}
