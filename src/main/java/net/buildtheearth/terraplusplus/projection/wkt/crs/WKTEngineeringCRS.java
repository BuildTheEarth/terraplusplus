package net.buildtheearth.terraplusplus.projection.wkt.crs;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTEngineeringDatum;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTEngineeringCRS extends AbstractWKTCRS {
    public static final WKTParseSchema<WKTEngineeringCRS> PARSE_SCHEMA = WKTParseSchema.builder(WKTEngineeringCRSBuilderImpl::new, WKTEngineeringCRSBuilder::build)
            .permitKeyword("LOCAL_CS")
            .requiredStringProperty(WKTEngineeringCRSBuilder::name)
            .requiredObjectProperty(WKTEngineeringDatum.PARSE_SCHEMA, WKTEngineeringCRSBuilder::datum)
            .requiredObjectProperty(WKTAngleUnit.PARSE_SCHEMA, WKTEngineeringCRSBuilder::unit)
            .addObjectListProperty(WKTAxis.PARSE_SCHEMA, WKTEngineeringCRSBuilder::axis, true)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final WKTEngineeringDatum datum;

    @NonNull
    private final WKTAngleUnit unit;

    @NonNull
    @Singular("axis")
    private final ImmutableList<WKTAxis> axes;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("LOCAL_CS")
                .writeQuotedLatinString(this.name)
                .writeRequiredObject(this.datum)
                .writeRequiredObject(this.unit)
                .writeObjectList(this.axes)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
