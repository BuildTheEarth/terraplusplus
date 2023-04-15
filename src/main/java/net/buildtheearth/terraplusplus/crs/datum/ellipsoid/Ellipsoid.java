package net.buildtheearth.terraplusplus.crs.datum.ellipsoid;

import lombok.Data;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.unit.Unit;

/**
 * @author DaPorkchop_
 */
@Data
public class Ellipsoid {
    public static Ellipsoid createSphere(@NonNull Unit lengthUnit, double radius) {
        return new Ellipsoid(lengthUnit, radius, radius, Double.POSITIVE_INFINITY, false);
    }

    public static Ellipsoid createFromAxes(@NonNull Unit lengthUnit, double semiMajorAxis, double semiMinorAxis) {
        return new Ellipsoid(lengthUnit, semiMajorAxis, semiMinorAxis, semiMajorAxis / (semiMajorAxis - semiMinorAxis), false);
    }

    public static Ellipsoid createFromInverseFlattening(@NonNull Unit lengthUnit, double semiMajorAxis, double inverseFlattening) {
        return new Ellipsoid(lengthUnit, semiMajorAxis, semiMajorAxis * (1.0d - 1.0d / inverseFlattening), inverseFlattening, true);
    }

    /**
     * The length unit used for the semi-major and semi-minor axes.
     */
    @NonNull
    private final Unit lengthUnit;

    private final double semiMajorAxis;
    private final double semiMinorAxis;
    private final double inverseFlattening;

    private final boolean definedByInverseFlattening;

    public double eccentricity() {
        double flattening = 1.0d - this.semiMinorAxis / this.semiMajorAxis;
        return Math.sqrt(2.0d * flattening - flattening * flattening);
    }

    public boolean isSphere() {
        return this.semiMajorAxis == this.semiMinorAxis;
    }
}
