package io.github.terra121.projection;

import java.util.HashMap;
import java.util.Map;

import io.github.terra121.TerraConstants;
import io.github.terra121.projection.airocean.Airocean;
import io.github.terra121.projection.airocean.ConformalEstimate;
import io.github.terra121.projection.airocean.ModifiedAirocean;
import io.github.terra121.projection.transform.InvertedOrientationProjectionTransform;
import io.github.terra121.projection.transform.UprightOrientationProjectionTransform;

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
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Equirectangular_projection">Wikipedia's article on the equirectangular projection</a>
 */
public abstract class GeographicProjection {

	/**
	 * Contains the various projections implemented in Terra121,
	 * identified by a String key.
	 */
	public static final Map<String, GeographicProjection> projections;

	static {
		projections = new HashMap<>();
		projections.put("web_mercator", new CenteredMapsProjection());
		projections.put("equirectangular", new EquirectangularProjection());
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
			base = new UprightOrientationProjectionTransform(base);
		}

		if (orientation == Orientation.swapped) {
			return new InvertedOrientationProjectionTransform(base);
		} else if (orientation == Orientation.upright) {
			base = new UprightOrientationProjectionTransform(base);
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
	 * @throws OutOfProjectionBoundsException if the specified point on the projected space cannot be mapped to a point of the geographic space
	 */
	public abstract double[] toGeo(double x, double y) throws OutOfProjectionBoundsException;

	/**
	 * Converts geographic coordinates to map coordinates
	 * 
	 * @param longitude - longitude, in degrees
	 * @param latitude - latitude, in degrees
	 * 
	 * @return {x, y} map coordinates
	 * @throws OutOfProjectionBoundsException if the specified point on the geographic space cannot be mapped to a point of the projected space
	 */
	public abstract double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException;

	/**
	 * Gives an estimation of the scale of this projection.
	 * This is just an estimation, as distortion is inevitable when projecting a sphere onto a flat surface,
	 * so this value varies from places to places in reality.
	 * 
	 * @return an estimation of the scale of this projection
	 */
	public abstract double metersPerUnit();

	/**
	 * Indicates the minimum and maximum X and Y coordinates on the projected space.
	 * 
	 * @return {minimum X, minimum Y, maximum X, maximum Y}
	 */
	public double[] bounds() {

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
			return new double[] {0, 0, 1, 1};
		}
	}

	/**
	 * Indicates whether or not the north pole is projected to the north of the south pole on the projected space,
	 * assuming Minecraft's coordinate system cardinal directions for the projected space (north is negative Z).
	 * 
	 * @return north pole Z <= south pole Z
	 */
	public boolean upright() {
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
	 * @param x - x coordinate in the projected space
	 * @param y - y coordinate in the projected space
	 * @param north - how far north to go, in meters on the geographic space
	 * @param east - how far east to go, in meters on the geographic space
	 * 
	 * @return {distance x, distance y} on the projected space
	 */
	public double[] vector(double x, double y, double north, double east) throws OutOfProjectionBoundsException {
		double[] geo = this.toGeo(x, y);

		//TODO: east may be slightly off because earth not a sphere
		double[] off = this.fromGeo(geo[0] + east * 360.0 / (Math.cos(Math.toRadians(geo[1])) * TerraConstants.EARTH_CIRCUMFERENCE),
				geo[1] + north * 360.0 / TerraConstants.EARTH_POLAR_CIRCUMFERENCE);

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
	 * @deprecated Prefer using {@link GeographicProjection#tissot(double, double)} for a default differential of 10^-7.
	 */
	@Deprecated
	public double[] tissot(double longitude, double latitude, double d) throws OutOfProjectionBoundsException {

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
	 * @see <a href="https://en.wikipedia.org/wiki/Tissot's_indicatrix">Wikipedia's article on Tissot's indicatrix</a>
	 * 
	 * @param longitude - longitude in degrees
	 * @param latitude - latitude in degrees
	 * 
	 * @return {area inflation, maximum angular distortion, maximum scale factor, minimum scale factor}
	 */
	public double[] tissot(double longitude, double latitude) throws OutOfProjectionBoundsException {
		return this.tissot(longitude,  latitude, 1E-7);
	}

	/**
	 * Converts an angle in the projected space to an azimuth in the geographic space, at a specific point.
	 * This is useful to get the direction an entity is looking at, i.e. it will be used by Terramap to show the direction entities are facing.
	 * With conformal projections, this should be equivalent to using {@link GeographicProjection#vector(double, double, double, double)} and computing the facing azimuth in the projected space,
	 * but on non-conformal projections angles are not preserved when projecting and this will be right when using {@link GeographicProjection#vector(double, double, double, double)} is likely to be wrong.
	 * 
	 * @param x - x coordinate of the point in the projected space
	 * @param y - y coordinate of the point in the projected space
	 * @param angle - the angle to convert, in degrees, in minecraft's coordinate system (angular origin at the positive side of the Z axis, positive clockwise)
	 * @param d - a length differential on the projected space
	 * 
	 * @return the corresponding azimuth, in degrees, counted positively clockwise, between 0° and 360°.
	 * @throws OutOfProjectionBoundsException if the given point is outside the projection domain
	 * @deprecated Prefer using {@link GeographicProjection#azimuth(double, double, float)} for a default differential of 10^-7. Smaller tends to give less accurate results.
	 */
	@Deprecated
	public float azimuth(double x, double y, float angle, double d) throws OutOfProjectionBoundsException {
		double x2 = x - d*Math.sin(Math.toRadians(angle));
		double y2 = y + d*Math.cos(Math.toRadians(angle));
		double[] geo1 = this.toGeo(x, y);
		double[] geo2 = this.toGeo(x2, y2);
		double dlon = geo2[0] - geo1[0];
		double dlat = geo2[1] - geo1[1];
		double a = Math.toDegrees(Math.atan2(dlat, dlon));
		a = 90 - a;
		if(a < 0) a += 360;
		return (float) a;
	}

	/**
	 * Converts an angle in the projected space to an azimuth in the geographic space, at a specific point.
	 * This is useful to get the direction an entity is looking at, i.e. it will be used by Terramap to show the direction entities are facing.
	 * With conformal projections, this should be equivalent to using {@link GeographicProjection#vector(double, double, double, double)} and computing the facing azimuth in the projected space,
	 * but on non-conformal projections angles are not preserved when projecting and this will be right when using {@link GeographicProjection#vector(double, double, double, double)} is likely to be wrong.
	 * 
	 * @param x - x coordinate of the point in the projected space
	 * @param y - y coordinate of the point in the projected space
	 * @param angle - the angle to convert, in degrees, in minecraft's coordinate system (angular origin at the positive side of the Z axis, positive clockwise)
	 * 
	 * @return the corresponding azimuth, in degrees, counted positively clockwise, between 0° and 360°.
	 * @throws OutOfProjectionBoundsException if the given point is outside the projection domain
	 */
	public float azimuth(double x, double y, float angle) throws OutOfProjectionBoundsException {
		return this.azimuth(x, y, angle, 1E-5);
	}

	public enum Orientation {
		none, upright, swapped
	}
}
