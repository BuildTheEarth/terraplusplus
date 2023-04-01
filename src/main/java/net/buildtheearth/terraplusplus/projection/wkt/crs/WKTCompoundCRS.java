package net.buildtheearth.terraplusplus.projection.wkt.crs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.cs.WKTCS;
import net.buildtheearth.terraplusplus.projection.wkt.projection.WKTProjection;

import java.io.IOException;
import java.util.List;

/**
 * @author DaPorkchop_
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTCompoundCRS extends WKTCRS {
    @NonNull
    private final List<WKTCRS> components;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("COMPOUNDCRS")
                .writeQuotedLatinString(this.name())
                .writeObjectList(this.components)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
