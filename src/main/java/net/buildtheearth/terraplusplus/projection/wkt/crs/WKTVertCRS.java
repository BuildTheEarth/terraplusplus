package net.buildtheearth.terraplusplus.projection.wkt.crs;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTVertDatum;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTVertCRS extends AbstractWKTCRS {
    public static final WKTParseSchema<WKTVertCRS> PARSE_SCHEMA = WKTParseSchema.builder(WKTVertCRSBuilderImpl::new, WKTVertCRSBuilder::build)
            .permitKeyword("VERT_CS")
            .requiredStringProperty(WKTVertCRSBuilder::name)
            .requiredObjectProperty(WKTVertDatum.PARSE_SCHEMA, WKTVertCRSBuilder::datum)
            .requiredObjectProperty(WKTAngleUnit.PARSE_SCHEMA, WKTVertCRSBuilder::unit)
            .addObjectListProperty(WKTAxis.PARSE_SCHEMA, WKTVertCRSBuilder::axis, true)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final WKTVertDatum datum;

    @NonNull
    private final WKTAngleUnit unit;

    @NonNull
    @Singular("axis")
    private final ImmutableList<WKTAxis> axes;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("VERT_CS")
                .writeQuotedLatinString(this.name)
                .writeRequiredObject(this.datum)
                .writeRequiredObject(this.unit)
                .writeObjectList(this.axes)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
