package io.github.terra121.projection.transform;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;

/**
 * Scales the warps projection's projected space up or down.
 * More specifically, it multiplies x and y by there respective scale factors.
 */
@JsonDeserialize
public class ScaleProjectionTransform extends ProjectionTransform {
    @JsonProperty("x")
    private final double scaleX;
    @JsonProperty("y")
    private final double scaleY;

    /**
     * Creates a new ScaleProjection with different scale factors for the x and y axis.
     *
     * @param delegate - projection to transform
     * @param x        - scaling to apply along the x axis
     * @param y        - scaling to apply along the y axis
     */
    @JsonCreator
    public ScaleProjectionTransform(
            @JsonProperty(value = "delegate", required = true) GeographicProjection delegate,
            @JsonProperty(value = "x", required = true) double x,
            @JsonProperty(value = "y", required = true) @JsonAlias("z") double y) {
        super(delegate);
        Preconditions.checkArgument(Double.isFinite(x) && Double.isFinite(y), "Projection scales should be finite");
        Preconditions.checkArgument(x != 0 && y != 0, "Projection scale cannot be 0!");
        this.scaleX = x;
        this.scaleY = y;
    }

    @Override
    public GeographicProjection optimize() {
        GeographicProjection optimizedDelegate = this.delegate.optimize();
        return this.scaleX == 1.0d && this.scaleY == 1.0d ? optimizedDelegate : new ScaleProjectionTransform(optimizedDelegate, this.scaleX, this.scaleY);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return this.delegate.toGeo(x / this.scaleX, y / this.scaleY);
    }

    @Override
    public double[] fromGeo(double lon, double lat) throws OutOfProjectionBoundsException {
        double[] p = this.delegate.fromGeo(lon, lat);
        p[0] *= this.scaleX;
        p[1] *= this.scaleY;
        return p;
    }

    @Override
    public boolean upright() {
        return (this.scaleY < 0) ^ this.delegate.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.delegate.bounds();
        b[0] *= this.scaleX;
        b[1] *= this.scaleY;
        b[2] *= this.scaleX;
        b[3] *= this.scaleY;
        return b;
    }

    @Override
    public double metersPerUnit() {
        return this.delegate.metersPerUnit() / Math.sqrt((this.scaleX * this.scaleX + this.scaleY * this.scaleY) / 2); //TODO: better transform
    }

    /**
     * @return the scaleX
     */
    public double getScaleX() {
        return scaleX;
    }

    /**
     * @return the scaleY
     */
    public double getScaleY() {
        return scaleY;
    }


}
