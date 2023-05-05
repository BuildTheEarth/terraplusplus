package net.buildtheearth.terraplusplus.projection.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.opengis.referencing.cs.CoordinateSystemAxis;

/**
 * Mirrors the warped projection vertically.
 * I.E. x' = x and y' = -y
 */
@JsonDeserialize
public class FlipVerticalProjectionTransform extends ProjectionTransform {
    /**
     * @param delegate - projection to transform
     */
    @JsonCreator
    public FlipVerticalProjectionTransform(
            @JsonProperty(value = "delegate", required = true) GeographicProjection delegate) {
        super(delegate);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return this.delegate.toGeo(x, -y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] p = this.delegate.fromGeo(longitude, latitude);
        p[1] = -p[1];
        return p;
    }

    @Override
    public boolean upright() {
        return !this.delegate.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.delegate.bounds();
        return new double[]{ b[0], -b[3], b[2], -b[1] };
    }

    @Override
    public String toString() {
        return "Vertical Flip (" + super.delegate + ')';
    }

    @Override
    protected String toSimpleString() {
        return "Vertical Flip";
    }

    @Override
    protected MatrixSIS affineMatrix() {
        return Matrices.createAffine(new Matrix2(1.0d, 0.0d, 0.0d, -1.0d), null);
    }
}
