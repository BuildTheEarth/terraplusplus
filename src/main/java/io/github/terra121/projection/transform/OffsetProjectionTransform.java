package io.github.terra121.projection.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;

/**
 * Applies a simple translation to the projected space, such that:
 * x' = x + offsetX and y' = y + offsetY
 */
@JsonDeserialize
public class OffsetProjectionTransform extends ProjectionTransform {
    private final double dx;
    private final double dy;

	/**
	 * @param delegate - Input projection
	 * @param dx - how much to move along the X axis
	 * @param dy - how much to move along the Y axis
	 */
	@JsonCreator
	public OffsetProjectionTransform(
            @JsonProperty(value = "delegate", required = true) GeographicProjection delegate,
            @JsonProperty(value = "dx", required = true) double dx,
            @JsonProperty(value = "dy", required = true) double dy) {
		super(delegate);
		Preconditions.checkArgument(Double.isFinite(dx) && Double.isFinite(dy), "Projection offsets have to be finite doubles");
		this.dx = dx;
		this.dy = dy;
	}

    @Override
    public GeographicProjection optimize() {
        GeographicProjection optimizedDelegate = this.delegate.optimize();
        return this.dx == 0.0d && this.dy == 0.0d ? optimizedDelegate : new OffsetProjectionTransform(optimizedDelegate, this.dx, this.dy);
    }

    @Override
	public double[] bounds() {
		double[] b = this.delegate.bounds();
		b[0] += this.dx;
		b[1] += this.dy;
		return b;
	}

	@Override
	public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
		return this.delegate.toGeo(x - this.dx, y - this.dy);
	}

	@Override
	public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
		double[] pos = this.delegate.fromGeo(longitude, latitude);
		pos[0] += this.dx;
		pos[1] += this.dy;
		return pos;
	}
}
