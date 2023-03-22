package net.buildtheearth.terraplusplus.projection.wkt.datum;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTEllipsoid;
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
public final class WKTDatum extends WKTObject.WithID {
    public static final WKTParseSchema<WKTDatum> PARSE_SCHEMA = WKTParseSchema.builder(WKTDatumBuilderImpl::new, WKTDatumBuilder::build)
            .permitKeyword("DATUM", "TRF", "GEODETICDATUM")
            .requiredStringProperty(WKTDatumBuilder::name)
            .requiredObjectProperty(WKTEllipsoid.PARSE_SCHEMA, WKTDatumBuilder::ellipsoid)
            .optionalObjectProperty(WKTTOWGS84.PARSE_SCHEMA, WKTDatumBuilder::toWGS84)
            .optionalObjectProperty(WKTDatumAnchor.PARSE_SCHEMA, WKTDatumBuilder::anchor)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @NonNull
    private final WKTEllipsoid ellipsoid;

    @Builder.Default
    private final WKTTOWGS84 toWGS84 = null;

    @Builder.Default
    private final WKTDatumAnchor anchor = null;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("DATUM")
                .writeQuotedLatinString(this.name)
                .writeRequiredObject(this.ellipsoid)
                .writeOptionalObject(this.toWGS84)
                .writeOptionalObject(this.anchor)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
