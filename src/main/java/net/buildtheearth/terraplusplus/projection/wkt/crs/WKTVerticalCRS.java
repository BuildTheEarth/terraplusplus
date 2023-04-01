package net.buildtheearth.terraplusplus.projection.wkt.crs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.cs.WKTCS;
import net.buildtheearth.terraplusplus.projection.wkt.datum.WKTVerticalDatum;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTVerticalCRS extends WKTGeographicCRS {
    @NonNull
    private final WKTVerticalDatum datum;

    //TODO: special datum_ensemble member

    @NonNull
    @JsonProperty("coordinate_system")
    private final WKTCS coordinateSystem;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("VERTCRS")
                .writeRequiredObject(this.datum)
                .writeRequiredObject(this.coordinateSystem)
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
}
