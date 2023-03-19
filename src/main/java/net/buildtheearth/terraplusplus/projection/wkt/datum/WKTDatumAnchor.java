package net.buildtheearth.terraplusplus.projection.wkt.datum;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTEllipsoid;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@Builder
@Data
@With
public final class WKTDatumAnchor {
    public static final WKTParseSchema<WKTDatumAnchor> PARSE_SCHEMA = WKTParseSchema.builder(WKTDatumAnchor::builder, WKTDatumAnchorBuilder::build)
            .permitKeyword("ANCHOR")
            .requiredStringProperty(WKTDatumAnchorBuilder::description)
            .build();

    @NonNull
    private final String description;
}
