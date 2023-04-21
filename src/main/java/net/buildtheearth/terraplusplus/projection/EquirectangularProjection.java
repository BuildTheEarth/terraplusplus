package net.buildtheearth.terraplusplus.projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
     * @param latitude  - latitude, in degrees
     * @return {x, y} map coordinates
     */
    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);
        return new double[]{ longitude, latitude };
    }

    @Override
    public CoordinateReferenceSystem projectedCRS() {
        return TerraConstants.TPP_GEO_CRS;
    }

    @Override
    public String toString() {
        return "Equirectangular";
    }
}
