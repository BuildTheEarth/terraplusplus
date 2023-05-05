package net.buildtheearth.terraplusplus.projection.sis.transform;

import lombok.Getter;
import lombok.NonNull;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.transform.ContextualParameters;
import org.opengis.parameter.ParameterValueGroup;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Getter
public abstract class AbstractToGeoMathTransform2D extends AbstractNormalizedMathTransform2D {
    private AbstractFromGeoMathTransform2D inverse;

    public AbstractToGeoMathTransform2D(@NonNull ParameterValueGroup contextualParameters) {
        super(contextualParameters);
    }

    void setInverse(@NonNull AbstractFromGeoMathTransform2D inverse) {
        checkState(this.inverse == null);
        checkArg(inverse.inverse() == this);
        this.inverse = inverse;
    }

    @Override
    protected void configureMatrices(ContextualParameters contextualParameters, MatrixSIS normalize, MatrixSIS denormalize) {
        contextualParameters.getMatrix(ContextualParameters.MatrixRole.NORMALIZATION).setMatrix(this.inverse().getContextualParameters().getMatrix(ContextualParameters.MatrixRole.INVERSE_DENORMALIZATION));
        contextualParameters.getMatrix(ContextualParameters.MatrixRole.DENORMALIZATION).setMatrix(this.inverse().getContextualParameters().getMatrix(ContextualParameters.MatrixRole.INVERSE_NORMALIZATION));
    }
}
