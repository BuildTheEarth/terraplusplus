package net.buildtheearth.terraplusplus.projection.sis;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.With;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.parameter.DefaultParameterValueGroup;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.transform.AbstractMathTransform2D;
import org.apache.sis.referencing.operation.transform.DomainDefinition;
import org.apache.sis.util.ComparisonMode;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import java.util.Optional;

import static net.buildtheearth.terraplusplus.projection.sis.WrappedProjectionOperationMethod.*;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WrappedProjectionMapTransform extends AbstractMathTransform2D {
    private final GeographicProjection projection;

    private final DefaultParameterValueGroup params;
    private transient volatile WrappedProjectionMapTransform inverse;

    @With(AccessLevel.PRIVATE)
    private final boolean fromGeo;

    public WrappedProjectionMapTransform(@NonNull ParameterValueGroup params) {
        this.params = new DefaultParameterValueGroup(params);

        String typeName = this.params.stringValue(PARAMETER_TYPE);
        Class<? extends GeographicProjection> type = GlobalParseRegistries.PROJECTIONS.get(typeName);
        if (type == null) {
            throw new InvalidParameterValueException("unknown projection type: \"" + typeName + '"', PARAMETER_TYPE.getName().getCode(), typeName);
        }

        String jsonArgs = this.params.stringValue(PARAMETER_JSON_ARGS);
        try {
            this.projection = TerraConstants.JSON_MAPPER.readValue(jsonArgs, type);
        } catch (JsonProcessingException e) {
            throw new InvalidParameterValueException("invalid projection arguments for type \"" + type + "\": \"" + jsonArgs + '"', PARAMETER_JSON_ARGS.getName().getCode(), jsonArgs);
        }

        this.fromGeo = true;
    }

    @Override
    public Optional<Envelope> getDomain(@NonNull DomainDefinition criteria) throws TransformException {
        double[] bounds = this.fromGeo ? this.projection.boundsGeo() : this.projection.bounds();

        Envelope2D envelope = new Envelope2D();
        envelope.add(bounds[0], bounds[1]);
        envelope.add(bounds[2], bounds[3]);
        return Optional.of(criteria.result().map(result -> envelope.createUnion(new Envelope2D(result))).orElse(envelope));
    }

    private double[] transform(double x, double y) throws OutOfProjectionBoundsException {
        return this.fromGeo ? this.projection.fromGeo(x, y) : this.projection.toGeo(x, y);
    }

    private Matrix2 derivative(double x, double y) throws OutOfProjectionBoundsException {
        return this.fromGeo ? this.projection.fromGeoDerivative(x, y) : this.projection.toGeoDerivative(x, y);
    }

    @Override
    public Matrix2 transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
        double srcX = srcPts[srcOff + 0];
        double srcY = srcPts[srcOff + 1];

        if (dstPts != null) { //may be null if only the derivative is requested
            double[] result00 = this.transform(srcX, srcY);

            dstPts[dstOff + 0] = result00[0];
            dstPts[dstOff + 1] = result00[1];
        }

        if (!derivate) {
            return null;
        }

        return this.derivative(srcX, srcY);
    }

    @Override
    public MathTransform2D inverse() {
        WrappedProjectionMapTransform inverse = this.inverse;
        if (inverse == null) {
            synchronized (this) {
                if ((inverse = this.inverse) == null) {
                    inverse = this.inverse = this.withFromGeo(!this.fromGeo);
                    inverse.inverse = this;
                }
            }
        }
        return inverse;
    }

    @Override
    public ParameterDescriptorGroup getParameterDescriptors() {
        return PARAMETERS;
    }

    @Override
    public ParameterValueGroup getParameterValues() {
        return this.params;
    }

    @Override
    public boolean equals(Object object, ComparisonMode mode) {
        return object instanceof WrappedProjectionMapTransform
               && super.equals(object, mode)
               && this.projection.equals(((WrappedProjectionMapTransform) object).projection)
               && this.fromGeo == ((WrappedProjectionMapTransform) object).fromGeo;
    }

    @Override
    protected int computeHashCode() {
        return (super.computeHashCode() * 31 + this.projection.hashCode()) * 31 + Boolean.hashCode(this.fromGeo);
    }
}
