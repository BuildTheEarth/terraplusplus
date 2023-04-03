package net.buildtheearth.terraplusplus.projection.wkt.datum;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public abstract class WKTVerticalDatum extends WKTDatum {
    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("VDATUM")
                .writeQuotedLatinString(this.name())
                //TODO: anchor
                .writeOptionalObject(this.id())
                .endObject();
    }
}
