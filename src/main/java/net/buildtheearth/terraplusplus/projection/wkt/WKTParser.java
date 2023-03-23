package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

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
            /*WKTAngleUnit.PARSE_SCHEMA,
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
            WKTVertDatum.PARSE_SCHEMA*/
    );

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
