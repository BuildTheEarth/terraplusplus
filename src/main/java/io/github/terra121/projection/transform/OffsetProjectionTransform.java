package io.github.terra121.projection.transform;

import com.google.common.base.Preconditions;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;

/**
 * Applies a simple translation to the projected space, such that:
 * x' = x + offsetX and y' = y + offsetY
 */
public class OffsetProjectionTransform extends ProjectionTransform {
	
	private final double deltaX, deltaY;
	
	/**
	 * @param input - Input projection
	 * @param deltaX - how much to move along the X axis
	 * @param deltaY - how much to move along the Y axis
	 */
	public OffsetProjectionTransform(GeographicProjection input, double deltaX, double deltaY) {
		super(input);
		Preconditions.checkArgument(Double.isFinite(deltaX) && Double.isFinite(deltaY), "Projection offsets have to be finite doubles");
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	@Override
	public double[] bounds() {
		double[] b = this.input.bounds();
		b[0] += this.deltaX;
		b[1] += this.deltaY;
		return b;
	}

	@Override
	public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
		return this.input.toGeo(x - this.deltaX, y - this.deltaY);
	}

	@Override
	public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
		double[] pos = this.input.fromGeo(longitude, latitude);
		pos[0] += this.deltaX;
		pos[1] += this.deltaY;
		return pos;
	}

	/**
	 * @return the deltaX
	 */
	public double getDeltaX() {
		return deltaX;
	}

	/**
	 * @return the deltaY
	 */
	public double getDeltaY() {
		return deltaY;
	}
	

}
