package net.buildtheearth.terraplusplus.projection.wkt.datum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTEllipsoid;
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
public final class WKTGeodeticDatumEnsemble extends WKTDatumEnsemble {
    @NonNull
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private final WKTEllipsoid ellipsoid;

    //TODO: prime meridian

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ENSEMBLE")
                .writeQuotedLatinString(this.name())
                .writeObjectList(this.members())
                .writeRequiredObject(this.ellipsoid)
                .beginObject("ENSEMBLEACCURACY").writeUnsignedNumericLiteral(this.accuracy()).endObject()
                .writeOptionalObject(this.id())
                .endObject();
    }
}
