package net.buildtheearth.terraplusplus.projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.sis.AbstractOperationMethod;
import net.buildtheearth.terraplusplus.projection.sis.AbstractSISMigratedGeographicProjection;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractFromGeoMathTransform2D;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractToGeoMathTransform2D;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.transform.ContextualParameters;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.TransformException;

/**
 * Implementation of the Sinusoidal projection.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Sinusoidal_projection"> Wikipedia's article on the sinusoidal projection</a>
 */
@JsonDeserialize
public class SinusoidalProjection extends AbstractSISMigratedGeographicProjection {
    @Override
    public double[] toGeo(double x, double y) {
        return new double[]{ x / Math.cos(Math.toRadians(y)), y };
    }

    @Override
    public Matrix2 toGeoDerivative(double x, double y) throws OutOfProjectionBoundsException {
        //https://www.wolframalpha.com/input?i=d%2Fdx+x+%2F+cos%28y+%2F+180+*+pi%29
        double m00 = (2.0d * Math.cos(Math.toRadians(y))) / (Math.cos(Math.toRadians(y * 2.0d)) + 1.0d);

        //https://www.wolframalpha.com/input?i=d%2Fdy+x+%2F+cos%28y+%2F+180+*+pi%29
        double m01 = (Math.PI * x * Math.sin(Math.toRadians(y))) / (90.0d * (Math.cos(Math.toRadians(y * 2.0d)) + 1.0d));
        double m10 = 0.0d;
        double m11 = 1.0d;

        return new Matrix2(m00, m01, m10, m11);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);
        return new double[]{ longitude * Math.cos(Math.toRadians(latitude)), latitude };
    }

    @Override
    public Matrix2 fromGeoDerivative(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);

        //https://www.wolframalpha.com/input?i=d%2Fdx+x+*+cos%28y+%2F+180+*+pi%29
        double m00 = Math.cos(Math.toRadians(latitude));

        //https://www.wolframalpha.com/input?i=d%2Fdy+x+*+cos%28y+%2F+180+*+pi%29
        double m01 = -Math.toRadians(longitude) * Math.sin(Math.toRadians(latitude));
        double m10 = 0.0d;
        double m11 = 1.0d;

        return new Matrix2(m00, m01, m10, m11);
    }

    @Override
    public double[] bounds() {
        return this.boundsGeo();
    }

    @Override
    public String toString() {
        return "Sinusoidal";
    }

    public static final class OperationMethod extends AbstractOperationMethod.ForLegacyProjection {
        public OperationMethod() {
            super("Sinusoidal");
        }

        @Override
        protected AbstractFromGeoMathTransform2D createBaseTransform(ParameterValueGroup parameters) throws InvalidParameterNameException, ParameterNotFoundException, InvalidParameterValueException {
            return new FromGeo(parameters);
        }
    }

    private static final class FromGeo extends AbstractFromGeoMathTransform2D {
        public FromGeo(@NonNull ParameterValueGroup parameters) {
            super(parameters, new ToGeo(parameters));
        }

        @Override
        protected void configureMatrices(ContextualParameters contextualParameters, MatrixSIS normalize, MatrixSIS denormalize) {
            //degrees -> radians
            contextualParameters.normalizeGeographicInputs(0.0d);
            contextualParameters.denormalizeGeographicOutputs(0.0d);
        }

        @Override
        public Matrix transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
            double lon = srcPts[srcOff + 0];
            double lat = srcPts[srcOff + 1];

            if (dstPts != null) {
                dstPts[dstOff + 0] = lon * Math.cos(lat);
                dstPts[dstOff + 1] = lat;
            }
            if (!derivate) {
                return null;
            }

            //https://www.wolframalpha.com/input?i=d%2Fdx+x+*+cos%28y%29
            double m00 = Math.cos(lat);

            //https://www.wolframalpha.com/input?i=d%2Fdy+x+*+cos%28y%29
            double m01 = -lon * Math.sin(lat);
            double m10 = 0.0d;
            double m11 = 1.0d;

            return new Matrix2(m00, m01, m10, m11);
        }
    }

    private static final class ToGeo extends AbstractToGeoMathTransform2D {
        public ToGeo(@NonNull ParameterValueGroup parameters) {
            super(parameters);
        }

        @Override
        public Matrix transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
            double x = srcPts[srcOff + 0];
            double y = srcPts[srcOff + 1];

            if (dstPts != null) {
                dstPts[dstOff + 0] = x / Math.cos(y);
                dstPts[dstOff + 1] = y;
            }
            if (!derivate) {
                return null;
            }

            //https://www.wolframalpha.com/input?i=d%2Fdx+x+%2F+cos%28y%29
            double m00 = (2.0d * Math.cos(y)) / (Math.cos(y * 2.0d) + 1.0d);

            //https://www.wolframalpha.com/input?i=d%2Fdy+x+%2F+cos%28y%29
            double m01 = (2.0d * x * Math.sin(y)) / ((Math.cos(y * 2.0d) + 1.0d));
            double m10 = 0.0d;
            double m11 = 1.0d;

            return new Matrix2(m00, m01, m10, m11);
        }
    }
}
