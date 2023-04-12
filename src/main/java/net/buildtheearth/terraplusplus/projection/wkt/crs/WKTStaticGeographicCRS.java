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
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTGeodeticDatumEnsemble;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTStaticGeographicCRS extends WKTGeographicCRS {
    @NonNull
    private final WKTDatum datum;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("GEOGCRS")
                .writeRequiredObject(this.datum)
                .writeRequiredObject(this.coordinateSystem())
                .writeOptionalObject(this.id())
                .endObject();
    }

    public abstract static class WKTStaticGeographicCRSBuilder<C extends WKTStaticGeographicCRS, B extends WKTStaticGeographicCRSBuilder<C, B>> extends WKTGeographicCRSBuilder<C, B> {
        @JsonProperty("datum_ensemble")
        @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
        public B datumEnsemble(@NonNull WKTGeodeticDatumEnsemble ensemble) {
            return this.datum(ensemble);
        }
    }
}
