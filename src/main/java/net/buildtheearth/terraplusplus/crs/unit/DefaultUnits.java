package net.buildtheearth.terraplusplus.crs.unit;

import lombok.experimental.UtilityClass;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public final class DefaultUnits {
    //
    // LENGTH
    //

    private static final Unit METER = BasicUnit.makeBase(UnitType.LENGTH, "Meter", "m").intern();

    public static Unit meter() {
        return METER;
    }

    public static Unit defaultLengthUnit() {
        return METER;
    }

    //
    // TIME
    //

    private static final Unit SECOND = BasicUnit.makeBase(UnitType.TIME, "Second", "s").intern();

    public static Unit second() {
        return SECOND;
    }

    public static Unit defaultTimeUnit() {
        return SECOND;
    }

    //
    // ANGLE
    //

    private static final Unit RADIAN = BasicUnit.makeBase(UnitType.ANGLE, "Radian", "rad").intern();

    private static final Unit DEGREE = RADIAN.multiply(Math.PI / 180.0d).withName("Degree").withSymbol("°").intern();

    public static Unit radian() {
        return RADIAN;
    }

    public static Unit degree() {
        return DEGREE;
    }

    public static Unit defaultAngleUnit() {
        return RADIAN;
    }
}
