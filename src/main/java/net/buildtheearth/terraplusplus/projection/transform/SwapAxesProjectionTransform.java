package net.buildtheearth.terraplusplus.projection.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.opengis.referencing.cs.CoordinateSystemAxis;

/**
 * Inverses the warped projection such that x becomes y and y becomes x.
 */
@JsonDeserialize
public class SwapAxesProjectionTransform extends ProjectionTransform {

    /**
     * @param delegate - projection to transform
     */
    @JsonCreator
    public SwapAxesProjectionTransform(
            @JsonProperty(value = "delegate", required = true) GeographicProjection delegate) {
        super(delegate);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return this.delegate.toGeo(y, x);
    }

    @Override
    public double[] fromGeo(double lon, double lat) throws OutOfProjectionBoundsException {
        double[] p = this.delegate.fromGeo(lon, lat);
        double t = p[0];
        p[0] = p[1];
        p[1] = t;
        return p;
    }

    @Override
    public double[] bounds() {
        double[] b = this.delegate.bounds();
        return new double[]{ b[1], b[0], b[3], b[2] };
    }

    @Override
    public String toString() {
        return "Swap Axes(" + super.delegate + ')';
    }

    @Override
    protected String toSimpleString() {
        return "Swap Axes";
    }

    @Override
    protected MatrixSIS affineMatrix() {
        return Matrices.createDimensionSelect(3, new int[]{ 1, 0, 2 });
    }

    @Override
    protected CoordinateSystemAxis[] transformAxes(CoordinateSystemAxis[] axes) {
        return new CoordinateSystemAxis[] { axes[0], axes[1] };
    }
}
