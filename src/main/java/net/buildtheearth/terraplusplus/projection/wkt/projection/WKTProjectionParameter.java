package net.buildtheearth.terraplusplus.projection.wkt.projection;

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
public final class WKTProjectionParameter extends WKTObject {
    public static final WKTParseSchema<WKTProjectionParameter> PARSE_SCHEMA = WKTParseSchema.builder(WKTProjectionParameterBuilderImpl::new, WKTProjectionParameterBuilder::build)
            .permitKeyword("PARAMETER")
            .requiredStringProperty(WKTProjectionParameterBuilder::name)
            .requiredSignedNumericProperty(WKTProjectionParameterBuilder::value)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final Number value;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("PARAMETER")
                .writeQuotedLatinString(this.name)
                .writeSignedNumericLiteral(this.value)
                .endObject();
    }
}
