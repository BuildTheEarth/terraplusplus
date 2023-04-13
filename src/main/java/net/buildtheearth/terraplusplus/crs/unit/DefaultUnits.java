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

    public static final Unit METER = BasicUnit.makeBase(UnitType.LENGTH, "Meter", "m").intern();

    //
    // TIME
    //

    public static final Unit SECOND = BasicUnit.makeBase(UnitType.TIME, "Second", "s").intern();

    //
    // ANGLE
    //

    public static final Unit RADIAN = BasicUnit.makeBase(UnitType.ANGLE, "Radian", "rad").intern();

    public static final Unit DEGREE = RADIAN.multiply(Math.PI / 180.0d).withName("Degree").withSymbol("°").intern();
}
