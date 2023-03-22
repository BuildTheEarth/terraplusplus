package net.buildtheearth.terraplusplus.projection.wkt.crs;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTPrimeMeridian;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTGeocentricCRS extends AbstractWKTCRS {
    public static final WKTParseSchema<WKTGeocentricCRS> PARSE_SCHEMA = WKTParseSchema.builder(WKTGeocentricCRSBuilderImpl::new, WKTGeocentricCRSBuilder::build)
            .permitKeyword("GEOCCS")
            .requiredStringProperty(WKTGeocentricCRSBuilder::name)
            .requiredObjectProperty(WKTDatum.PARSE_SCHEMA, WKTGeocentricCRSBuilder::datum)
            .requiredObjectProperty(WKTPrimeMeridian.PARSE_SCHEMA, WKTGeocentricCRSBuilder::primeMeridian)
            .requiredObjectProperty(WKTLengthUnit.PARSE_SCHEMA, WKTGeocentricCRSBuilder::linearUnit)
            .addObjectListProperty(WKTAxis.PARSE_SCHEMA, WKTGeocentricCRSBuilder::axis, true)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final WKTDatum datum;

    @NonNull
    private final WKTPrimeMeridian primeMeridian;

    @NonNull
    private final WKTLengthUnit linearUnit;

    @NonNull
    @Singular("axis")
    private final ImmutableList<WKTAxis> axes;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("GEOCCS")
                .writeQuotedLatinString(this.name)
                .writeRequiredObject(this.datum)
                .writeRequiredObject(this.primeMeridian)
                .writeRequiredObject(this.linearUnit)
                .writeObjectList(this.axes)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
