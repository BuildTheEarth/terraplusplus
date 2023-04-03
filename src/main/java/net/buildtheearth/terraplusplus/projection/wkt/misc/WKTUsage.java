package net.buildtheearth.terraplusplus.projection.wkt.misc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.misc.extent.WKTExtent;
import net.buildtheearth.terraplusplus.projection.wkt.misc.extent.WKTGeographicBoundingBox;
import net.buildtheearth.terraplusplus.projection.wkt.misc.extent.WKTVerticalExtent;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTUsage extends AbstractWKTObject {
    @NonNull
    private final String scope;

    @Builder.Default
    private final String area = null;

    private final WKTGeographicBoundingBox bbox;

    @JsonProperty("vertical_extent")
    private final WKTVerticalExtent verticalExtent;

    //TODO: temporal extent

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("USAGE")
                .beginObject("SCOPE").writeQuotedLatinString(this.scope).endObject();

        if (this.area != null) {
            writer.beginObject("AREA").writeQuotedLatinString(this.area).endObject();
        }

        writer.writeOptionalObject(this.bbox)
                .writeOptionalObject(this.verticalExtent)
                .endObject();
    }
}
