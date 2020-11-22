package io.github.terra121.projection;

/**
 * Implementation of the universal transverse Mercator projection.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Universal_Transverse_Mercator_coordinate_system">Wikipedia's article on the universal transverse Mercator projection</a>
 * @see <a href="https://en.wikipedia.org/wiki/Transverse_Mercator_projection">Wikipedia's article on the transverse Mercator projection</a>
 */
public class TransverseMercatorProjection extends GeographicProjection {
    public static final double zoneWidth = Math.toRadians(6.0);
    private static final double metersPerUnit = EARTH_CIRCUMFERENCE / (2 * Math.PI);

    /**
     * @param longitude - longitude in radians
     * @return the central meridian to use when projecting at the given longitude, in radians
     */
    public static double getCentralMeridian(double longitude) {
    	//TODO Why is there a Math.floor here? It seems to work a lot better without it
        return (Math.floor(longitude / zoneWidth) + 0.5) * zoneWidth;
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) {
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
    public double[] toGeo(double x, double y) {
        double centralMeridian = getCentralMeridian(x);
        x -= centralMeridian;
        double lam = Math.atan2(Math.sinh(x), Math.cos(y)) + centralMeridian;
        double phi = Math.asin(Math.sin(y) / Math.cosh(x));
        double lon = Math.toDegrees(lam);
        double lat = Math.toDegrees(phi);
        return new double[]{ lon, lat };
    }

    @Override
    public double metersPerUnit() {
        return metersPerUnit;
    }
}
