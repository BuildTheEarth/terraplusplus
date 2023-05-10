package net.buildtheearth.terraplusplus.projection;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.sis.referencing.operation.matrix.Matrix2;

/**
 * Static helper methods, intended for use by implementations of {@link GeographicProjection}.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class GeographicProjectionHelper {
    public static final double DEFAULT_DERIVATIVE_DELTA = 1e-9d;

    public static Matrix2 defaultDerivative(@NonNull GeographicProjection projection, double x, double y, boolean fromGeo) throws OutOfProjectionBoundsException {
        return defaultDerivative(projection, x, y, fromGeo, DEFAULT_DERIVATIVE_DELTA);
    }

    public static Matrix2 defaultDerivative(@NonNull GeographicProjection projection, double x, double y, boolean fromGeo, double d) throws OutOfProjectionBoundsException {
        double[] result00 = project(projection, x, y, fromGeo);
        
        double x00 = result00[0];
        double y00 = result00[1];

        double inverseD = 1.0d / d;

        double f01;
        double[] result01;
        try {
            f01 = inverseD;
            result01 = project(projection, x, y + d, fromGeo);
        } catch (OutOfProjectionBoundsException e) {
            f01 = -inverseD;
            result01 = project(projection, x, y - d, fromGeo);
        }

        double x01 = result01[0];
        double y01 = result01[1];

        double f10;
        double[] result10;
        try {
            f10 = inverseD;
            result10 = project(projection, x + d, y, fromGeo);
        } catch (OutOfProjectionBoundsException e) {
            f10 = -inverseD;
            result10 = project(projection, x - d, y, fromGeo);
        }

        double x10 = result10[0];
        double y10 = result10[1];

        return new Matrix2(
                (x10 - x00) * f10,
                (x01 - x00) * f01,
                (y10 - y00) * f10,
                (y01 - y00) * f01);
    }

    public static double[] project(@NonNull GeographicProjection projection, double x, double y, boolean fromGeo) throws OutOfProjectionBoundsException {
        return fromGeo ? projection.fromGeo(x, y) : projection.toGeo(x, y);
    }
}
