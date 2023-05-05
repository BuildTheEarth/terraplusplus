package net.buildtheearth.terraplusplus.projection.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Getter;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.Matrix3;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.opengis.referencing.cs.CoordinateSystemAxis;

/**
 * Scales the warps projection's projected space up or down.
 * More specifically, it multiplies x and y by there respective scale factors.
 */
@JsonDeserialize
@Getter(onMethod_ = { @JsonGetter })
public class ScaleProjectionTransform extends ProjectionTransform {
    private final double x;
    private final double y;

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
            @JsonProperty(value = "y", required = true) double y) {
        super(delegate);
        Preconditions.checkArgument(Double.isFinite(x) && Double.isFinite(y), "Projection scales should be finite");
        Preconditions.checkArgument(x != 0 && y != 0, "Projection scale cannot be 0!");
        this.x = x;
        this.y = y;
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return this.delegate.toGeo(x / this.x, y / this.y);
    }

    @Override
    public double[] fromGeo(double lon, double lat) throws OutOfProjectionBoundsException {
        double[] p = this.delegate.fromGeo(lon, lat);
        p[0] *= this.x;
        p[1] *= this.y;
        return p;
    }

    @Override
    public boolean upright() {
        return (this.y < 0) ^ this.delegate.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.delegate.bounds();
        b[0] *= this.x;
        b[1] *= this.y;
        b[2] *= this.x;
        b[3] *= this.y;
        return b;
    }

    @Override
    public String toString() {
        return "Scale (" + super.delegate + ") by " + this.x + ", " + this.y;
    }

    @Override
    protected String toSimpleString() {
        return "Scale by " + this.x + ", " + this.y;
    }

    @Override
    protected MatrixSIS affineMatrix() {
        return Matrices.createAffine(new Matrix2(this.x, 0.0d, 0.0d, this.y), null);
    }
}
