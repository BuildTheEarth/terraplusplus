package net.buildtheearth.terraminusminus.projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraminusminus.TerraConstants;
import net.buildtheearth.terraminusminus.config.GlobalParseRegistries;
import net.buildtheearth.terraminusminus.config.TypedDeserializer;
import net.buildtheearth.terraminusminus.config.TypedSerializer;
import net.buildtheearth.terraminusminus.util.MathUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Support for various projection types.
 * <p>
 * The geographic space is the surface of the earth, parameterized by the usual spherical coordinates system of latitude and longitude.
 * The projected space is a plane on to which the geographic space is being projected, and is parameterized by a 2D Cartesian coordinate system (x and y).
 * <p>
 * A projection as defined here is something that projects a point in the geographic space to a point of the projected space (and vice versa).
 * <p>
 * All geographic coordinates are in degrees.
 *
 */
@JsonDeserialize(using = GeographicProjection.Deserializer.class)
@JsonSerialize(using = GeographicProjection.Serializer.class)
public interface GeographicProjection {
    @SneakyThrows(IOException.class)
    static GeographicProjection parse(@NonNull String config) {
        return TerraConstants.JSON_MAPPER.readValue(config, GeographicProjection.class);
    }

    /**
     * Converts map coordinates to geographic coordinates
     *
     * @param x - x map coordinate
     * @param y - y map coordinate
     * @return {longitude, latitude} in degrees
     * @throws OutOfProjectionBoundsException if the specified point on the projected space cannot be mapped to a point of the geographic space
     */
    double[] toGeo(double x, double y) throws OutOfProjectionBoundsException;

    /**
     * Converts geographic coordinates to map coordinates
     *
     * @param longitude - longitude, in degrees
     * @param latitude  - latitude, in degrees
     * @return {x, y} map coordinates
     * @throws OutOfProjectionBoundsException if the specified point on the geographic space cannot be mapped to a point of the projected space
     */
    double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException;

    /**
     * Gives an estimation of the scale of this projection.
     * This is just an estimation, as distortion is inevitable when projecting a sphere onto a flat surface,
     * so this value varies from places to places in reality.
     *
     * @return an estimation of the scale of this projection
     */
    double metersPerUnit();

    /**
     * Indicates the minimum and maximum X and Y coordinates on the projected space.
     *
     * @return {minimum X, minimum Y, maximum X, maximum Y}
     */
    default double[] bounds() {
        try {
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
        } catch (OutOfProjectionBoundsException e) {
            return new double[]{ 0, 0, 1, 1 };
        }
    }

    /**
     * Indicates whether or not the north pole is projected to the north of the south pole on the projected space,
     * assuming Minecraft's coordinate system cardinal directions for the projected space (north is negative Z).
     *
     * @return north pole Z &lt;= south pole Z
     */
    default boolean upright() {
        try {
            return this.fromGeo(0, 90)[1] <= this.fromGeo(0, -90)[1];
        } catch (OutOfProjectionBoundsException e) {
            return false;
        }
    }

    /**
     * Calculates the vector that goes a given distance north and a given distance east from the given point in the projected space.
     * This is useful to get a direction in the projected space, e.g. it is used to calculate the north vector used when sending eyes of ender.
     *
     * @param x     - x coordinate in the projected space
     * @param y     - y coordinate in the projected space
     * @param north - how far north to go, in meters on the geographic space
     * @param east  - how far east to go, in meters on the geographic space
     * @return {distance x, distance y} on the projected space
     */
    default double[] vector(double x, double y, double north, double east) throws OutOfProjectionBoundsException {
        double[] geo = this.toGeo(x, y);

        //TODO: east may be slightly off because earth not a sphere
        double[] off = this.fromGeo(geo[0] + east * 360.0 / (Math.cos(Math.toRadians(geo[1])) * TerraConstants.EARTH_CIRCUMFERENCE),
                geo[1] + north * 360.0 / TerraConstants.EARTH_POLAR_CIRCUMFERENCE);

        return new double[]{ off[0] - x, off[1] - y };
    }

    /**
     * Computes the Tissot's indicatrix of this projection at the given point (i.e. the distortion).
     *
     * @param longitude - longitude in degrees
     * @param latitude  - latitude in degrees
     * @param d         - a length differential in meters (a small quantity used to approximate partial derivatives)
     * @return {area inflation, maximum angular distortion, maximum scale factor, minimum scale factor}
     * @see <a href="https://en.wikipedia.org/wiki/Tissot's_indicatrix">Wikipedia's article on Tissot's indicatrix</a>
     * @deprecated Prefer using {@link GeographicProjection#tissot(double, double)} for a default differential of 10^-7.
     */
    @Deprecated
    default double[] tissot(double longitude, double latitude, double d) throws OutOfProjectionBoundsException {

        double R = TerraConstants.EARTH_CIRCUMFERENCE / (2 * Math.PI);

        double ddeg = Math.toDegrees(d);

        double[] base = this.fromGeo(longitude, latitude);
        double[] lonoff = this.fromGeo(longitude + ddeg, latitude);
        double[] latoff = this.fromGeo(longitude, latitude + ddeg);

        double dxdl = (lonoff[0] - base[0]) / d;
        double dxdp = (latoff[0] - base[0]) / d;
        double dydl = (lonoff[1] - base[1]) / d;
        double dydp = (latoff[1] - base[1]) / d;

        double cosp = Math.cos(Math.toRadians(latitude));

        double h = Math.sqrt(dxdp * dxdp + dydp * dydp) / R;
        double k = Math.sqrt(dxdl * dxdl + dydl * dydl) / (cosp * R);

        double sint = Math.abs(dydp * dxdl - dxdp * dydl) / (R * R * cosp * h * k);
        double ap = Math.sqrt(h * h + k * k + 2 * h * k * sint);
        double bp = Math.sqrt(h * h + k * k - 2 * h * k * sint);

        double a = (ap + bp) / 2;
        double b = (ap - bp) / 2;

        return new double[]{ h * k * sint, 2 * Math.asin(bp / ap), a, b };
    }

    /**
     * Computes the Tissot's indicatrix of this projection at the given point (i.e. the distortion).
     *
     * @param longitude - longitude in degrees
     * @param latitude  - latitude in degrees
     * @return {area inflation, maximum angular distortion, maximum scale factor, minimum scale factor}
     * @see <a href="https://en.wikipedia.org/wiki/Tissot's_indicatrix">Wikipedia's article on Tissot's indicatrix</a>
     */
    default double[] tissot(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return this.tissot(longitude, latitude, 1E-7d);
    }

    /**
     * Converts an angle in the projected space to an azimuth in the geographic space, at a specific point.
     * This is useful to get the direction an entity is looking at, i.e. it will be used by Terramap to show the direction entities are facing.
     * With conformal projections, this should be equivalent to using {@link GeographicProjection#vector(double, double, double, double)} and computing the facing azimuth in the projected space,
     * but on non-conformal projections angles are not preserved when projecting and this will be right when using {@link GeographicProjection#vector(double, double, double, double)} is likely to be wrong.
     *
     * @param x     - x coordinate of the point in the projected space
     * @param y     - y coordinate of the point in the projected space
     * @param angle - the angle to convert, in degrees, in minecraft's coordinate system (angular origin at the positive side of the Z axis, positive clockwise)
     * @param d     - a length differential on the projected space
     * @return the corresponding azimuth, in degrees, counted positively clockwise, between 0° and 360°.
     * @throws OutOfProjectionBoundsException if the given point is outside the projection domain
     * @deprecated Prefer using {@link GeographicProjection#azimuth(double, double, float)} for a default differential of 10^-7. Smaller tends to give less accurate results.
     */
    @Deprecated
    default float azimuth(double x, double y, float angle, double d) throws OutOfProjectionBoundsException {
        double x2 = x - d * Math.sin(Math.toRadians(angle));
        double y2 = y + d * Math.cos(Math.toRadians(angle));
        double[] geo1 = this.toGeo(x, y);
        double[] geo2 = this.toGeo(x2, y2);
        MathUtils.toRadians(geo1);
        MathUtils.toRadians(geo2);
        double dlon = geo2[0] - geo1[0];
        double dlat = geo2[1] - geo1[1];
        double a = Math.toDegrees(Math.atan2(dlat, dlon*Math.cos(geo1[1])));
        a = 90 - a;
        if (a < 0) {
            a += 360;
        }
        return (float) a;
    }

    /**
     * Converts an angle in the projected space to an azimuth in the geographic space, at a specific point.
     * This is useful to get the direction an entity is looking at, i.e. it will be used by Terramap to show the direction entities are facing.
     * With conformal projections, this should be equivalent to using {@link GeographicProjection#vector(double, double, double, double)} and computing the facing azimuth in the projected space,
     * but on non-conformal projections angles are not preserved when projecting and this will be right when using {@link GeographicProjection#vector(double, double, double, double)} is likely to be wrong.
     *
     * @param x     - x coordinate of the point in the projected space
     * @param y     - y coordinate of the point in the projected space
     * @param angle - the angle to convert, in degrees, in minecraft's coordinate system (angular origin at the positive side of the Z axis, positive clockwise)
     * @return the corresponding azimuth, in degrees, counted positively clockwise, between 0° and 360°.
     * @throws OutOfProjectionBoundsException if the given point is outside the projection domain
     */
    default float azimuth(double x, double y, float angle) throws OutOfProjectionBoundsException {
        return this.azimuth(x, y, angle, 1E-5);
    }

    /**
     * @return any additional configuration properties used by this projection
     */
    default Map<String, Object> properties() {
        return Collections.emptyMap();
    }

    class Deserializer extends TypedDeserializer<GeographicProjection> {
        @Override
        protected Map<String, Class<? extends GeographicProjection>> registry() {
            return GlobalParseRegistries.PROJECTIONS;
        }
    }

    class Serializer extends TypedSerializer<GeographicProjection> {
        @Override
        protected Map<Class<? extends GeographicProjection>, String> registry() {
            return GlobalParseRegistries.PROJECTIONS.inverse();
        }
    }
}
