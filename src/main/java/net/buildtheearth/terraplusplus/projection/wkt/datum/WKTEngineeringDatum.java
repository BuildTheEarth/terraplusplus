package net.buildtheearth.terraplusplus.projection.wkt.datum;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTEngineeringDatum extends WKTObject.WithID {
    public static final WKTParseSchema<WKTEngineeringDatum> PARSE_SCHEMA = WKTParseSchema.builder(WKTEngineeringDatumBuilderImpl::new, WKTEngineeringDatumBuilder::build)
            .permitKeyword("LOCAL_DATUM")
            .requiredStringProperty(WKTEngineeringDatumBuilder::name)
            .requiredSignedNumericProperty(WKTEngineeringDatumBuilder::type)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final Number type;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("LOCAL_DATUM")
                .writeQuotedLatinString(this.name)
                .writeSignedNumericLiteral(this.type)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
