package net.buildtheearth.terraplusplus.projection.wkt.misc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTPrimeMeridian extends WKTObject.WithNameAndID {
    @NonNull
    private final Number longitude;

    //!! If <angle unit> is omitted, the <signed numeric literal> value must be in the CRS's CS angular unit if available, else in decimal degrees.
    @Builder.Default
    private final WKTAngleUnit angleUnit = null;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("PRIMEM")
                .writeQuotedLatinString(this.name())
                .writeSignedNumericLiteral(this.longitude)
                .writeOptionalObject(this.angleUnit)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
