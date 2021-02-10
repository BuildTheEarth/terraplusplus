package net.buildtheearth.terraplusplus.projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Implements the equirectangular map projection, which applies no transformation at all.
 * x and y are therefore the same as longitude and latitude (in degrees).
 */
@JsonDeserialize
public class EquirectangularProjection implements GeographicProjection {
    /**
     * Converts map coordinates to geographic coordinates
     * 
     * @param x - x map coordinate
     * @param y - y map coordinate
     * 
     * @return {longitude, latitude} in degrees
     */
    @Override
	public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
    	OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(x, y);
        return new double[]{ x, y };
    }

    /**
     * Converts geographic coordinates to map coordinates
     * 
     * @param longitude - longitude, in degrees
     * @param latitude - latitude, in degrees
     * 
     * @return {x, y} map coordinates
     */
    @Override
	public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);
        return new double[]{ longitude, latitude };
    }

    /**
     * Gives an estimation of the scale of this projection.
     * This is just an estimation, as distortion is inevitable when projecting a sphere onto a flat surface,
     * so this value varies from places to places in reality.
     * 
     * @return an estimation of the scale of this projection
     */
    @Override
	public double metersPerUnit() {
        return 100000;
    }

    @Override
    public String toString() {
        return "Equirectangular";
    }
}
