package net.buildtheearth.terraplusplus.crs.axis;

import lombok.Getter;

/**
 * @author DaPorkchop_
 */
@Getter
public enum CoordinateAxis {
    NORTH,
    NORTH_NORTH_EAST,
    NORTH_EAST,
    EAST_NORTH_EAST,
    EAST,
    EAST_SOUTH_EAST,
    SOUTH_EAST,
    SOUTH_SOUTH_EAST,
    SOUTH(NORTH),
    SOUTH_SOUTH_WEST(NORTH_NORTH_EAST),
    SOUTH_WEST(NORTH_EAST),
    WEST_SOUTH_WEST(EAST_NORTH_EAST),
    WEST(EAST),
    WEST_NORTH_WEST(EAST_SOUTH_EAST),
    NORTH_WEST(SOUTH_EAST),
    NORTH_NORTH_WEST(SOUTH_SOUTH_EAST),
    UP,
    DOWN(UP),
    GEOCENTRIC_X,
    GEOCENTRIC_Y,
    GEOCENTRIC_Z,
    FUTURE,
    PAST,
    COLUMN_POSITIVE,
    COLUMN_NEGATIVE(COLUMN_POSITIVE),
    ROW_POSITIVE,
    ROW_NEGATIVE(ROW_POSITIVE),
    DISPLAY_RIGHT,
    DISPLAY_LEFT(DISPLAY_RIGHT),
    DISPLAY_UP,
    DISPLAY_DOWN(DISPLAY_UP),
    ;

    private final CoordinateAxis opposite;

    /**
     * This axis' absolute direction.
     */
    private final CoordinateAxis absolute;

    CoordinateAxis() {
        this(null);
    }

    CoordinateAxis(CoordinateAxis opposite) {
        this.opposite = opposite;

        this.absolute = opposite != null && opposite.ordinal() < this.ordinal() ? opposite : this;
    }
}
