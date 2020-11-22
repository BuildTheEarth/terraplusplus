package io.github.terra121.projection;

/**
 * Mirrors the warped projection vertically.
 * I.E. x' = x and y' = -y
 */
public class UprightOrientation extends ProjectionTransform {

	/**
	 * @param input - projection to transform
	 */
    public UprightOrientation(GeographicProjection input) {
        super(input);
    }

    @Override
    public double[] toGeo(double x, double y) {
        return this.input.toGeo(x, -y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) {
        double[] p = this.input.fromGeo(longitude, latitude);
        p[1] = -p[1];
        return p;
    }

    @Override
    public boolean upright() {
        return !this.input.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.input.bounds();
        return new double[]{ b[0], -b[3], b[2], -b[1] };
    }
}
