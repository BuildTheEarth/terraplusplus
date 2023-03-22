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
public final class WKTProjection extends WKTObject.WithID {
    public static final WKTParseSchema<WKTProjection> PARSE_SCHEMA = WKTParseSchema.builder(WKTProjectionBuilderImpl::new, WKTProjectionBuilder::build)
            .permitKeyword("PROJECTION")
            .requiredStringProperty(WKTProjectionBuilder::name)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("PROJECTION")
                .writeQuotedLatinString(this.name)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
