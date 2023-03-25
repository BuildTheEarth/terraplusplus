package net.buildtheearth.terraplusplus.projection.wkt.projection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTProjectionParameter extends WKTObject.WithNameAndID {
    @NonNull
    private final Number value;

    @Builder.Default
    private final WKTUnit unit = null;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("PARAMETER")
                .writeQuotedLatinString(this.name())
                .writeSignedNumericLiteral(this.value)
                .writeOptionalObject(this.unit)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
