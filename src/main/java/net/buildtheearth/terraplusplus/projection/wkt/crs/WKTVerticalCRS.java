package net.buildtheearth.terraplusplus.projection.wkt.crs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTDatum;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTVerticalDatumEnsemble;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTVerticalCRS extends WKTCRS.WithCoordinateSystem {
    @NonNull
    private final WKTDatum datum;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("VERTCRS")
                .writeRequiredObject(this.datum)
                .writeRequiredObject(this.coordinateSystem())
                .writeOptionalObject(this.id())
                .endObject();
    }

    /**
     * @author DaPorkchop_
     */
    @Jacksonized
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static final class GeoidModel extends AbstractWKTObject.WithNameAndID {
        @Override
        public void write(@NonNull WKTWriter writer) throws IOException {
            writer.beginObject("GEOIDMODEL")
                    .writeQuotedLatinString(this.name())
                    .writeOptionalObject(this.id())
                    .endObject();
        }
    }

    public abstract static class WKTVerticalCRSBuilder<C extends WKTVerticalCRS, B extends WKTVerticalCRSBuilder<C, B>> extends WithCoordinateSystemBuilder<C, B> {
        @JsonProperty("datum_ensemble")
        @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
        public B datumEnsemble(@NonNull WKTVerticalDatumEnsemble ensemble) {
            return this.datum(ensemble);
        }
    }
}
