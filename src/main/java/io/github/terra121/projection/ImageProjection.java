package io.github.terra121.projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Implements the equirecangular projection but transforms it so that all coordinates on the projected space are positive.
 * 
 * @see EquirectangularProjection
 */
@JsonDeserialize
public class ImageProjection extends EquirectangularProjection {
    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        this.checkArgs(x, y);
        return new double[]{ x - 180, 90 - y };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        this.checkArgs(longitude, latitude);
        return new double[]{ longitude + 180, 90 - latitude };
    }
}
