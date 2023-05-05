package net.buildtheearth.terraplusplus.projection.sis.transform;

import lombok.NonNull;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.transform.AbstractMathTransform2D;
import org.apache.sis.referencing.operation.transform.ContextualParameters;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.util.FactoryException;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractNormalizedMathTransform2D extends AbstractMathTransform2D {
    private final ContextualParameters contextualParameters;

    private boolean configuredMatrices;

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
}
