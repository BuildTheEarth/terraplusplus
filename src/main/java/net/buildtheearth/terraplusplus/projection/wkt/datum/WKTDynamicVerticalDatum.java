package net.buildtheearth.terraplusplus.projection.wkt.datum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTDynamicVerticalDatum extends WKTVerticalDatum {
    @NonNull
    @JsonProperty("frame_reference_epoch")
    private final Number frameReferenceEpoch;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("DYNAMIC")
                .beginObject("FRAMEEPOCH")
                .writeUnsignedNumericLiteral(this.frameReferenceEpoch)
                .endObject()
                .endObject();

        super.write(writer);
    }
}
