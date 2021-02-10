package net.buildtheearth.terraplusplus.projection.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

/**
 * Mirrors the warped projection horizontally.
 * I.E. x' = -x and y' = y
 */
@JsonDeserialize
public class FlipHorizontalProjectionTransform extends ProjectionTransform {
    /**
     * @param delegate - projection to transform
     */
    @JsonCreator
    public FlipHorizontalProjectionTransform(
            @JsonProperty(value = "delegate", required = true) GeographicProjection delegate) {
        super(delegate);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return this.delegate.toGeo(-x, y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] p = this.delegate.fromGeo(longitude, latitude);
        p[0] = -p[0];
        return p;
    }

    @Override
    public boolean upright() {
        return !this.delegate.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.delegate.bounds();
        return new double[]{ -b[0], b[3], -b[2], b[1] };
    }

    @Override
    public String toString() {
        return "Horizontal Flip (" + super.delegate + ')';
    }
}
