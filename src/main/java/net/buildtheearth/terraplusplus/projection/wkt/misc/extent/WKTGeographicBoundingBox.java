package net.buildtheearth.terraplusplus.projection.wkt.misc.extent;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTGeographicBoundingBox extends WKTExtent {
    @JsonProperty("south_latitude")
    private final double southLatitude;

    @JsonProperty("west_longitude")
    private final double westLongitude;

    @JsonProperty("north_latitude")
    private final double northLatitude;

    @JsonProperty("east_longitude")
    private final double eastLongitude;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("BBOX")
                .writeSignedNumericLiteral(this.southLatitude)
                .writeSignedNumericLiteral(this.westLongitude)
                .writeSignedNumericLiteral(this.northLatitude)
                .writeSignedNumericLiteral(this.eastLongitude)
                .endObject();
    }
}
