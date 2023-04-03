package net.buildtheearth.terraplusplus.projection.wkt.cs;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTAngleUnit;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTAxis extends AbstractWKTObject.WithID {
    @Builder.Default
    private final String name = null; //TODO: at most one of name/abbreviation may be null

    @Builder.Default
    private final String abbreviation = null;

    @NonNull
    private final Direction direction;

    @Builder.Default
    private final WKTUnit unit = null; //TODO: should be a spatial unit

    @Builder.Default
    private final Meridian meridian = null; //TODO: only allowed when direction is 'north' or 'south'

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
                .writeOptionalObject(this.meridian)
                .writeOptionalObject(this.id())
                .endObject();
    }

    /**
     * @author DaPorkchop_
     */
    public enum Direction {
        north, northNorthEast, northEast, eastNorthEast, east, eastSouthEast, southEast, southSouthEast, south, southSouthWest, southWest, westSouthWest, west, westNorthWest, northWest, northNorthWest, geocentricX, geocentricY, geocentricZ, up, down, forward, aft, port, starboard, clockwise, counterClockwise, columnPositive, columnNegative, rowPositive, rowNegative, displayRight, displayLeft, displayUp, displayDown, future, past, towards, awayFrom, unspecified,
    }

    /**
     * @author DaPorkchop_
     */
    @Jacksonized
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static final class Meridian extends AbstractWKTObject {
        @NonNull
        private final Double longitude;

        @NonNull
        @Builder.Default
        private final WKTAngleUnit unit = WKTAngleUnit.DEGREE; //TODO: figure out why this field is required in WKT2, but omitted in PROJJSON

        @Override
        public void write(@NonNull WKTWriter writer) throws IOException {
            writer.beginObject("MERIDIAN")
                    .writeSignedNumericLiteral(this.longitude)
                    .writeRequiredObject(this.unit)
                    .endObject();
        }
    }
}
