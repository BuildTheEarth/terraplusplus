package net.buildtheearth.terraplusplus.projection.sis.transform;

import lombok.Getter;
import lombok.NonNull;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author DaPorkchop_
 */
@Getter
public abstract class AbstractFromGeoMathTransform2D extends AbstractNormalizedMathTransform2D {
    private final AbstractToGeoMathTransform2D inverse;

    public AbstractFromGeoMathTransform2D(@NonNull ParameterValueGroup contextualParameters, @NonNull AbstractToGeoMathTransform2D inverse) {
        super(contextualParameters);

        this.inverse = inverse;
        inverse.setInverse(this);
    }
}
