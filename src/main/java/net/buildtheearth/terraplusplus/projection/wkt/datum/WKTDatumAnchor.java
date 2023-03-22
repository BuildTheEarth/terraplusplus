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
public final class WKTDatumAnchor extends WKTObject {
    public static final WKTParseSchema<WKTDatumAnchor> PARSE_SCHEMA = WKTParseSchema.builder(WKTDatumAnchorBuilderImpl::new, WKTDatumAnchorBuilder::build)
            .permitKeyword("ANCHOR")
            .requiredStringProperty(WKTDatumAnchorBuilder::description)
            .build();

    @NonNull
    private final String description;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ANCHOR")
                .writeQuotedLatinString(this.description)
                .endObject();
    }
}
