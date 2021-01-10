package io.github.terra121.projection.transform;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;

/**
 * Inverses the warped projection such that x becomes y and y becomes x.
 */
public class InvertedOrientationProjectionTransform extends ProjectionTransform {

	/**
	 * @param input - projection to transform
	 */
    public InvertedOrientationProjectionTransform(GeographicProjection input) {
        super(input);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return this.input.toGeo(y, x);
    }

    @Override
    public double[] fromGeo(double lon, double lat) throws OutOfProjectionBoundsException {
        double[] p = this.input.fromGeo(lon, lat);
        double t = p[0];
        p[0] = p[1];
        p[1] = t;
        return p;
    }

    @Override
    public double[] bounds() {
        double[] b = this.input.bounds();
        return new double[]{ b[1], b[0], b[3], b[2] };
    }
}
