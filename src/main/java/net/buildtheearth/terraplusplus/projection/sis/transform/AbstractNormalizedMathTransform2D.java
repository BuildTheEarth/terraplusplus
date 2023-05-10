package net.buildtheearth.terraplusplus.projection.sis.transform;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.transform.AbstractMathTransform2D;
import org.apache.sis.referencing.operation.transform.ContextualParameters;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.util.Arrays;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractNormalizedMathTransform2D extends AbstractMathTransform2D {
    private final ContextualParameters contextualParameters;
    private boolean configuredMatrices;

    private volatile OutOfProjectionBoundsException cachedBulkException;

    public AbstractNormalizedMathTransform2D(@NonNull ParameterValueGroup contextualParameters) {
        this.contextualParameters = new ContextualParameters(contextualParameters.getDescriptor(), 2, 2);

        //copy parameters into our ContextualParameters instance
        for (GeneralParameterValue v : contextualParameters.values()) {
            ParameterValue<?> value = (ParameterValue<?>) v;
            if (value.getUnit() != null) {
                this.contextualParameters.parameter(value.getDescriptor().getName().getCode()).setValue(value.doubleValue(), value.getUnit());
            } else {
                this.contextualParameters.parameter(value.getDescriptor().getName().getCode()).setValue(value.getValue());
            }
        }
    }

    protected abstract void configureMatrices(ContextualParameters contextualParameters, MatrixSIS normalize, MatrixSIS denormalize);

    public synchronized MathTransform completeTransform(MathTransformFactory factory) throws FactoryException {
        if (!this.configuredMatrices) {
            this.configuredMatrices = true;
            this.configureMatrices(this.contextualParameters,
                    this.contextualParameters.getMatrix(ContextualParameters.MatrixRole.NORMALIZATION),
                    this.contextualParameters.getMatrix(ContextualParameters.MatrixRole.DENORMALIZATION));
        }

        return this.contextualParameters.completeTransform(factory, this);
    }

    @Override
    protected ContextualParameters getContextualParameters() {
        return this.contextualParameters;
    }

    @Override
    public abstract Matrix2 transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException;

    protected static boolean isLongitudeLatitudeInRange(double longitude, double latitude) {
        return Math.abs(longitude) <= 180.0d && Math.abs(latitude) <= 90.0d;
    }

    protected static boolean isInvalidCoordinates(double x, double y) {
        return Double.isNaN(x) || Double.isNaN(y);
    }

    /**
     * Gets an {@link OutOfProjectionBoundsException} to be thrown when this transform fails to project an individual point.
     * <p>
     * The object's {@link OutOfProjectionBoundsException#getLastCompletedTransform() last completed transform} will be equal to {@code null}.
     */
    protected final OutOfProjectionBoundsException getSingleExceptionForSelf() {
        return OutOfProjectionBoundsException.get();
    }

    /**
     * Gets an {@link OutOfProjectionBoundsException} to be thrown when this transform fails to project some number of points of a bulk transformation,
     * such that all failed points have a value of {@link Double#NaN NaN} in the destination array.
     * <p>
     * The object's {@link OutOfProjectionBoundsException#getLastCompletedTransform() last completed transform} will be equal to {@code this}.
     */
    protected final OutOfProjectionBoundsException getBulkExceptionForSelf() {
        if (OutOfProjectionBoundsException.FAST) { //lazily allocate a new exception instance if required, or rethrow existing cached exception
            OutOfProjectionBoundsException exception = this.cachedBulkException;
            if (exception != null) {
                return exception;
            }

            exception = new OutOfProjectionBoundsException();
            exception.setLastCompletedTransform(this);
            this.cachedBulkException = exception;
            return exception;
        } else { //always create a new exception
            OutOfProjectionBoundsException exception = new OutOfProjectionBoundsException();
            exception.setLastCompletedTransform(this);
            return exception;
        }
    }

    protected static void fillNaN(double[] dstPts, int dstOff) {
        if (dstPts != null) {
            Arrays.fill(dstPts, dstOff, dstOff + 2, Double.NaN);
        }
    }
}
