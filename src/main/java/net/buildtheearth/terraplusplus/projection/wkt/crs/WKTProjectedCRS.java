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
import net.buildtheearth.terraplusplus.projection.wkt.projection.WKTProjection;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTProjectedCRS extends WKTCRS.WithCoordinateSystem {
    @NonNull
    @JsonProperty("base_crs")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private final WKTStaticGeographicCRS baseCrs; //TODO: is actually 'a GeodeticCRS, which is generally a GeographicCRS'

    @NonNull
    private final WKTProjection conversion;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("PROJCRS")
                .writeQuotedLatinString(this.name())
                .writeRequiredObject(this.baseCrs)
                .writeRequiredObject(this.conversion)
                .writeRequiredObject(this.coordinateSystem())
                .writeOptionalObject(this.id())
                .endObject();
    }
}
