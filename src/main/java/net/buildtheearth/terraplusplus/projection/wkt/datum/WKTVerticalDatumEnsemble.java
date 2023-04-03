package net.buildtheearth.terraplusplus.projection.wkt.datum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public final class WKTVerticalDatumEnsemble extends WKTDatumEnsemble {
    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ENSEMBLE")
                .writeQuotedLatinString(this.name())
                .writeObjectList(this.members())
                .writeOptionalObject(this.id())
                .endObject();
    }
}
