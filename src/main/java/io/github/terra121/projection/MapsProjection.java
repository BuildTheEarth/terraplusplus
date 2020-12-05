package io.github.terra121.projection;

import io.github.terra121.util.MathUtils;

/**
 * Implementation of the Mercator projection, with projected space is normalized between 0 and 1 (as with the web Mercator variant).
 * The Greenwich meridian and the equator are at x=0 and y=0 respectively.
 * 
 * @see io.github.terra121.projection.CenteredMapsProjection
 * @see <a href="https://en.wikipedia.org/wiki/Mercator_projection"> Wikipedia's article on the Mercator projection</a>
 * @see <a href="https://en.wikipedia.org/wiki/Web_Mercator_projection"> Wikipedia's article on the Web Mercator projection</a>
 */
public class MapsProjection extends GeographicProjection {

    @Override
    public double[] toGeo(double x, double y) {
        return new double[]{
                Math.toDegrees(x * MathUtils.TAU - Math.PI),
                Math.toDegrees(Math.atan(Math.exp(Math.PI - y * MathUtils.TAU)) * 2 - Math.PI / 2)
        };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) {
        return new double[]{
                (Math.toRadians(longitude) + Math.PI) / MathUtils.TAU,
                (Math.PI - Math.log(Math.tan((Math.PI / 2 + Math.toRadians(latitude)) / 2))) / MathUtils.TAU
        };
    }

    @Override
    public double[] bounds() {
        return new double[]{ 0, 0, 1, 1};
    }

    @Override
    public boolean upright() {
        return true;
    }

	@Override
	public double metersPerUnit() {
		return 100000;
	}
}
