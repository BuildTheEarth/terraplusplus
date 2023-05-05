package net.buildtheearth.terraplusplus.projection.sis.transform;

import lombok.Getter;
import lombok.NonNull;
import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.referencing.operation.transform.DomainDefinition;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.TransformException;

import java.util.Optional;

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

    @Override
    public Optional<Envelope> getDomain(@NonNull DomainDefinition criteria) throws TransformException {
        return Optional.of(new Envelope2D(new DefaultGeographicBoundingBox(-180.0d, 180.0d, -90.0d, 90.0d)));
    }
}
