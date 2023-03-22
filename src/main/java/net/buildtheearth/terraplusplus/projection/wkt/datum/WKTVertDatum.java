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
public final class WKTVertDatum extends WKTObject.WithID {
    public static final WKTParseSchema<WKTVertDatum> PARSE_SCHEMA = WKTParseSchema.builder(WKTVertDatumBuilderImpl::new, WKTVertDatumBuilder::build)
            .permitKeyword("VERT_DATUM")
            .requiredStringProperty(WKTVertDatumBuilder::name)
            .requiredSignedNumericProperty(WKTVertDatumBuilder::type)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final Number type;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("VERT_DATUM")
                .writeQuotedLatinString(this.name)
                .writeSignedNumericLiteral(this.type)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
