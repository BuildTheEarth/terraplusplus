package net.buildtheearth.terraplusplus.projection.wkt.projection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;
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
public final class WKTProjectionMethod extends AbstractWKTObject.WithNameAndID {
    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("METHOD")
                .writeQuotedLatinString(this.name())
                .writeOptionalObject(this.id())
                .endObject();
    }
}
