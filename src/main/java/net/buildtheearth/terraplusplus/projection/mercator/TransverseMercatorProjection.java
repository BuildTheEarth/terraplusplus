package net.buildtheearth.terraplusplus.projection.mercator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

/**
 * Implementation of the universal transverse Mercator projection.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Universal_Transverse_Mercator_coordinate_system">Wikipedia's article on the universal transverse Mercator projection</a>
 * @see <a href="https://en.wikipedia.org/wiki/Transverse_Mercator_projection">Wikipedia's article on the transverse Mercator projection</a>
 */
@JsonDeserialize
public class TransverseMercatorProjection implements GeographicProjection {
    /**
     * Width of a longitude range in radians.
     * The 360 degrees of longitude are divided into chunks of this size,
     * and each zone gets its own central meridian to use for the universal projection.
     */
    public static final double ZONE_WIDTH = Math.toRadians(6.0);

    private static final double METERS_PER_UNIT = TerraConstants.EARTH_CIRCUMFERENCE / (2 * Math.PI);

    /**
     * @param longitude - longitude in radians
     * @return the central meridian to use when projecting at the given longitude, in radians
     */
    public static double getCentralMeridian(double longitude) {
        //TODO Why is there a Math.floor here? It seems to work a lot better without it
        return (Math.floor(longitude / ZONE_WIDTH) + 0.5) * ZONE_WIDTH;
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);
        double lam = Math.toRadians(longitude);
        double phi = Math.toRadians(latitude);
        double centralMeridian = getCentralMeridian(lam);
        lam -= centralMeridian;

        double b = Math.cos(phi) * Math.sin(lam);
        double x = Math.log((1.0 + b) / (1.0 - b)) / 2;
        double y = Math.atan2(Math.tan(phi), Math.cos(lam));
        x += centralMeridian;
        return new double[]{ x, y };
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(x, y, Math.PI, Math.PI / 2);
        double centralMeridian = getCentralMeridian(x);
        x -= centralMeridian;
        double lam = Math.atan2(Math.sinh(x), Math.cos(y)) + centralMeridian;
        double phi = Math.asin(Math.sin(y) / Math.cosh(x));
        double lon = Math.toDegrees(lam);
        double lat = Math.toDegrees(phi);
        return new double[]{ lon, lat };
    }

    @Override
    public String toString() {
        return "Transverse Mercator";
    }
}
