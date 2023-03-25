package net.buildtheearth.terraplusplus.projection.wkt.cs;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTAxis extends WKTObject.WithID {
    @Builder.Default
    private final String name = null; //TODO: at most one of name/abbreviation may be null

    @Builder.Default
    private final String abbreviation = null;

    @NonNull
    private final Direction direction;

    @Builder.Default
    private final WKTUnit unit = null; //TODO: should be a spatial unit

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("AXIS");

        if (this.name != null && this.abbreviation != null) {
            writer.writeEnumName('"' + this.name.replace("\"", "\"\"") + "\" " + this.abbreviation);
        } else if (this.name != null) {
            writer.writeQuotedLatinString(this.name);
        } else if (this.abbreviation != null) {
            writer.writeEnumName('(' + this.abbreviation + ')');
        }

        writer.writeOptionalObject(this.unit)
                .writeOptionalObject(this.id())
                .endObject();
    }

    /**
     * @author DaPorkchop_
     */
    public enum Direction {
        north, northNorthEast, northEast, eastNorthEast, east, eastSouthEast, southEast, southSouthEast, south, southSouthWest, southWest, westSouthWest, west, westNorthWest, northWest, northNorthWest, geocentricX, geocentricY, geocentricZ, up, down, forward, aft, port, starboard, clockwise, counterClockwise, columnPositive, columnNegative, rowPositive, rowNegative, displayRight, displayLeft, displayUp, displayDown, future, past, towards, awayFrom, unspecified,
    }
}
