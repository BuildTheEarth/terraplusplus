package net.buildtheearth.terraplusplus.projection.wkt.misc.extent;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTVerticalExtent extends WKTExtent {
    private final double minimumHeight;

    private final double maximumHeight;

    @NonNull
    @Builder.Default
    private final WKTLengthUnit unit = WKTLengthUnit.METRE;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("VERTICALEXTENT")
                .writeSignedNumericLiteral(this.minimumHeight)
                .writeSignedNumericLiteral(this.maximumHeight)
                .writeOptionalObject(this.unit)
                .endObject();
    }
}
