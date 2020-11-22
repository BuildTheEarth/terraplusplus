package io.github.terra121.projection;

/**
 * Implements the equirecangular projection but transforms it so that all coordinates on the projected space are positive.
 */
public class ImageProjection extends GeographicProjection {
	
    @Override
    public double[] toGeo(double x, double y) {
        return new double[]{ x - 180, 90 - y };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) {
        return new double[]{ longitude + 180, 90 - latitude };
    }
    
}
