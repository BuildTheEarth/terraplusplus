package io.github.terra121.projection;

/**
 * Implementation of the Sinusoidal projection.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Sinusoidal_projection"> Wikipedia's article on the sinusoidal projection</a>
 *
 */
public class SinusoidalProjection extends GeographicProjection {

    private static final double TO_RADIANS = Math.PI / 180.0;

    @Override
    public double[] toGeo(double x, double y) {
        return new double[]{ x / Math.cos(y * TO_RADIANS), y };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) {
        return new double[]{ longitude * Math.cos(latitude * TO_RADIANS), latitude };
    }

    @Override
    public double metersPerUnit() {
        return EARTH_CIRCUMFERENCE / 360.0; //gotta make good on that exact area
    }
}
