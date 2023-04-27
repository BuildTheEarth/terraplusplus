package net.buildtheearth.terraplusplus.projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.util.TerraUtils;
import org.apache.sis.referencing.operation.matrix.Matrix2;

/**
 * Implementation of the Sinusoidal projection.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Sinusoidal_projection"> Wikipedia's article on the sinusoidal projection</a>
 */
@JsonDeserialize
public class SinusoidalProjection implements GeographicProjection {
    @Override
    public double[] toGeo(double x, double y) {
        return new double[]{ x / Math.cos(Math.toRadians(y)), y };
    }

    @Override
    public Matrix2 toGeoDerivative(double x, double y) throws OutOfProjectionBoundsException {
        //https://www.wolframalpha.com/input?i=d%2Fdx+x+%2F+cos%28y+%2F+180+*+pi%29
        double m00 = (2.0d * Math.cos(Math.toRadians(y))) / (Math.cos(Math.toRadians(y * 2.0d)) + 1.0d);

        //https://www.wolframalpha.com/input?i=d%2Fdy+x+%2F+cos%28y+%2F+180+*+pi%29
        double m01 = (Math.PI * x * Math.sin(Math.toRadians(y))) / (90.0d * (Math.cos(Math.toRadians(y * 2.0d)) + 1.0d));
        double m10 = 0.0d;
        double m11 = 1.0d;

        return new Matrix2(m00, m01, m10, m11);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);
        return new double[]{ longitude * Math.cos(Math.toRadians(latitude)), latitude };
    }

    @Override
    public Matrix2 fromGeoDerivative(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);

        //https://www.wolframalpha.com/input?i=d%2Fdx+x+*+cos%28y+%2F+180+*+pi%29
        double m00 = Math.cos(Math.toRadians(latitude));

        //https://www.wolframalpha.com/input?i=d%2Fdy+x+*+cos%28y+%2F+180+*+pi%29
        double m01 = -Math.toRadians(longitude) * Math.sin(Math.toRadians(latitude));
        double m10 = 0.0d;
        double m11 = 1.0d;

        return new Matrix2(m00, m01, m10, m11);
    }

    @Override
    public double[] bounds() {
        return this.boundsGeo();
    }

    @Override
    public String toString() {
        return "Sinusoidal";
    }
}
