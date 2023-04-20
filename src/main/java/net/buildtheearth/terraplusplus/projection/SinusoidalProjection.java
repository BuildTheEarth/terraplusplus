package net.buildtheearth.terraplusplus.projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
    	OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);
        return new double[]{ longitude * Math.cos(Math.toRadians(latitude)), latitude };
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
