package net.buildtheearth.terraplusplus.projection.wkt.crs;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTPrimeMeridian;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/18-010r7/18-010r7.html#209">WKT Specification §C.4.1: Geodetic CRS</a>
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTStaticGeographicCRS extends WKTObject.WithID {
    public static final WKTParseSchema<WKTStaticGeographicCRS> PARSE_SCHEMA = WKTParseSchema.builder(WKTStaticGeographicCRSBuilderImpl::new, WKTStaticGeographicCRSBuilder::build)
            .permitKeyword("GEOGCS")
            .requiredStringProperty(WKTStaticGeographicCRSBuilder::name)
            .requiredObjectProperty(WKTDatum.PARSE_SCHEMA, WKTStaticGeographicCRSBuilder::datum)
            .requiredObjectProperty(WKTPrimeMeridian.PARSE_SCHEMA, WKTStaticGeographicCRSBuilder::primeMeridian)
            //TODO: according to https://docs.opengeospatial.org/is/18-010r7/18-010r7.html#209 AXIS comes before UNIT, but
            // https://docs.geotools.org/stable/javadocs/org/opengis/referencing/doc-files/WKT.html claims it comes after
            .addObjectListProperty(WKTAxis.PARSE_SCHEMA, WKTStaticGeographicCRSBuilder::axis, true)
            .requiredObjectProperty(WKTAngleUnit.PARSE_SCHEMA, WKTStaticGeographicCRSBuilder::angleUnit)
            .addObjectListProperty(WKTAxis.PARSE_SCHEMA, WKTStaticGeographicCRSBuilder::axis, true)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final WKTDatum datum;

    @NonNull
    private final WKTPrimeMeridian primeMeridian;

    @NonNull
    private final WKTAngleUnit angleUnit;

    @NonNull
    @Singular("axis")
    private final ImmutableList<WKTAxis> axes;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("GEOGCS")
                .writeQuotedLatinString(this.name)
                .writeRequiredObject(this.datum)
                .writeRequiredObject(this.primeMeridian)
                .writeRequiredObject(this.angleUnit)
                .writeObjectList(this.axes)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
