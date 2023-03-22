package net.buildtheearth.terraplusplus.projection.wkt.crs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParser;
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
public final class WKTCompdCRS extends AbstractWKTCRS {
    public static final WKTParseSchema<WKTCompdCRS> PARSE_SCHEMA = WKTParseSchema.builder(WKTCompdCRSBuilderImpl::new, WKTCompdCRSBuilder::build)
            .permitKeyword("COMPD_CS")
            .requiredStringProperty(WKTCompdCRSBuilder::name)
            .addAnyObjectProperty(() -> WKTParser.CRS_SCHEMAS, WKTCompdCRSBuilder::headCRS, false)
            .addAnyObjectProperty(() -> WKTParser.CRS_SCHEMAS, WKTCompdCRSBuilder::tailCRS, false)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final AbstractWKTCRS headCRS;

    @NonNull
    private final AbstractWKTCRS tailCRS;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("COMPD_CS")
                .writeQuotedLatinString(this.name)
                .writeRequiredObject(this.headCRS)
                .writeRequiredObject(this.tailCRS)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
