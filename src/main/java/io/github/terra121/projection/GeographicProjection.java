package io.github.terra121.projection;

import io.github.terra121.projection.airocean.Airocean;
import io.github.terra121.projection.airocean.ConformalEstimate;
import io.github.terra121.projection.airocean.ModifiedAirocean;
import io.github.terra121.projection.transform.InvertedOrientation;
import io.github.terra121.projection.transform.UprightOrientation;

import java.util.HashMap;
import java.util.Map;

import io.github.terra121.TerraConstants;

/**
 * Support for various projection types.
 * 
 * The geographic space is the surface of the earth, parameterized by the usual spherical coordinates system of latitude and longitude.
 * The projected space is a plane on to which the geographic space is being projected, and is parameterized by a 2D Cartesian coordinate system (x and y).
 * 
 * A projection as defined here is something that projects a point in the geographic space to a point of the projected space (and vice versa).
 * 
 * All geographic coordinates are in degrees.
 * 
 * This base class applies no transformation so longitude and latitude are the same as x and y (equirectangular projection).
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Equirectangular_projection">Wikipedia's article on the equirectangular projection</a>
 */
public class GeographicProjection {

    public static final double EARTH_CIRCUMFERENCE = TerraConstants.EARTH_CIRCUMFERENCE;
    public static final double EARTH_POLAR_CIRCUMFERENCE = TerraConstants.EARTH_POLAR_CIRCUMFERENCE;

    /**
     * Contains the various projections implemented in Terra121,
     * identified by a String key.
     */
    public static final Map<String, GeographicProjection> projections;

    static {
        projections = new HashMap<>();
        projections.put("web_mercator", new CenteredMapsProjection());
        projections.put("equirectangular", new GeographicProjection());
        projections.put("sinusoidal", new SinusoidalProjection());
        projections.put("equal_earth", new EqualEarth());
        projections.put("airocean", new Airocean());
        projections.put("transverse_mercator", new TransverseMercatorProjection());
        projections.put("airocean", new Airocean());
        projections.put("conformal", new ConformalEstimate());
        projections.put("bteairocean", new ModifiedAirocean());
    }

    /**
     * Orients a projection
     * 
     * @param base - the projection to orient
     * @param orientation - the orientation to use
     * 
     * @return a projection that warps the base projection but applies the transformation described by the given orientation
     */
    public static GeographicProjection orientProjection(GeographicProjection base, Orientation orientation) {
        if (base.upright()) {
            if (orientation == Orientation.upright) {
                return base;
            }
            base = new UprightOrientation(base);
        }

        if (orientation == Orientation.swapped) {
            return new InvertedOrientation(base);
        } else if (orientation == Orientation.upright) {
            base = new UprightOrientation(base);
        }

        return base;
    }

    /**
     * Converts map coordinates to geographic coordinates
     * 
     * @param x - x map coordinate
     * @param y - y map coordinate
     * 
     * @return {longitude, latitude} in degrees
     */
    public double[] toGeo(double x, double y) {
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
    public double[] fromGeo(double longitude, double latitude) {
        return new double[]{ longitude, latitude };
    }

    /**
     * Gives an estimation of the scale of this projection.
     * This is just an estimation, as distortion is inevitable when projecting a sphere onto a flat surface,
     * so this value varies from places to places in reality.
     * 
     * @return an estimation of the scale of this projection
     */
    public double metersPerUnit() {
        return 100000;
    }

    /**
     * Indicates the minimum and maximum X and Y coordinates on the projected space.
     * 
     * @return {minimum X, minimum Y, maximum X, maximum Y}
     */
    public double[] bounds() {

        //get max in by using extreme coordinates
        double[] bounds = {
                this.fromGeo(-180, 0)[0],
                this.fromGeo(0, -90)[1],
                this.fromGeo(180, 0)[0],
                this.fromGeo(0, 90)[1]
        };

        if (bounds[0] > bounds[2]) {
            double t = bounds[0];
            bounds[0] = bounds[2];
            bounds[2] = t;
        }

        if (bounds[1] > bounds[3]) {
            double t = bounds[1];
            bounds[1] = bounds[3];
            bounds[3] = t;
        }

        return bounds;
    }

    /**
     * Indicates whether or not the north pole is projected to the north of the south pole on the projected space,
     * assuming Minecraft's coordinate system cardinal directions for the projected space (north is negative Z).
     * 
     * @return north pole Z <= south pole Z
     */
    public boolean upright() {
        return this.fromGeo(0, 90)[1] <= this.fromGeo(0, -90)[1];
    }

    /**
     * Calculates the vector that goes a given distance north and a given distance east from the given point in the projected space.
     * 
     * @param x - x coordinate in the projected space
     * @param y - y coordinate in the projected space
     * @param north - how far north to go, in meters on the geographic space
     * @param east - how far east to go, in meters on the geographic space
     * 
     * @return {distance x, distance y} on the projected space
     */
    public double[] vector(double x, double y, double north, double east) {
        double[] geo = this.toGeo(x, y);

        //TODO: east may be slightly off because earth not a sphere
        double[] off = this.fromGeo(geo[0] + east * 360.0 / (Math.cos(geo[1] * Math.PI / 180.0) * EARTH_CIRCUMFERENCE),
                geo[1] + north * 360.0 / EARTH_POLAR_CIRCUMFERENCE);

        return new double[]{ off[0] - x, off[1] - y };
    }

    /**
     * Computes the Tissot's indicatrix of this projection at the given point (i.e. the distortion).
     * 
     * @see <a href="https://en.wikipedia.org/wiki/Tissot's_indicatrix">Wikipedia's article on Tissot's indicatrix</a>
     * 
     * @param longitude - longitude in degrees
     * @param latitude - latitude in degrees
     * @param d - a length differential in meters (a small quantity used to approximate partial derivatives)
     * 
     * @return {area inflation, maximum angular distortion, maximum scale factor, minimum scale factor}
     */
    public double[] tissot(double longitude, double latitude, double d) {

        double R = EARTH_CIRCUMFERENCE / (2 * Math.PI);

        double ddeg = d * 180.0 / Math.PI;

        double[] base = this.fromGeo(longitude, latitude);
        double[] lonoff = this.fromGeo(longitude + ddeg, latitude);
        double[] latoff = this.fromGeo(longitude, latitude + ddeg);

        double dxdl = (lonoff[0] - base[0]) / d;
        double dxdp = (latoff[0] - base[0]) / d;
        double dydl = (lonoff[1] - base[1]) / d;
        double dydp = (latoff[1] - base[1]) / d;

        double cosp = Math.cos(latitude * Math.PI / 180.0);

        double h = Math.sqrt(dxdp * dxdp + dydp * dydp) / R;
        double k = Math.sqrt(dxdl * dxdl + dydl * dydl) / (cosp * R);

        double sint = Math.abs(dydp * dxdl - dxdp * dydl) / (R * R * cosp * h * k);
        double ap = Math.sqrt(h * h + k * k + 2 * h * k * sint);
        double bp = Math.sqrt(h * h + k * k - 2 * h * k * sint);

        double a = (ap + bp) / 2;
        double b = (ap - bp) / 2;

        return new double[]{ h * k * sint, 2 * Math.asin(bp / ap), a, b };
    }

    public enum Orientation {
        none, upright, swapped
    }
}
