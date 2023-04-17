package net.buildtheearth.terraplusplus.projection.dymaxion;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.referencing.operation.projection.ProjectionException;
import org.apache.sis.referencing.operation.transform.AbstractMathTransform;
import org.apache.sis.referencing.operation.transform.DomainDefinition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.TransformException;

import java.util.Optional;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class InverseDymaxionProjectionMathTransform extends AbstractMathTransform {
    private final DymaxionProjection projection = new DymaxionProjection();

    private final boolean fromGeo;

    @Override
    public int getSourceDimensions() {
        return 2;
    }

    @Override
    public int getTargetDimensions() {
        return 2;
    }

    @Override
    public Optional<Envelope> getDomain(@NonNull DomainDefinition criteria) throws TransformException {
        Envelope2D envelope;

        if (this.fromGeo) {
            double[] geoBounds = this.projection.boundsGeo();

            envelope = new Envelope2D();
            envelope.add(geoBounds[0], geoBounds[1]);
            envelope.add(geoBounds[2], geoBounds[2]);
        } else {
            double[] projectedBounds = this.projection.bounds();

            envelope = new Envelope2D();
            envelope.add(projectedBounds[0], projectedBounds[1]);
            envelope.add(projectedBounds[2], projectedBounds[2]);
        }

        return Optional.of(criteria.result().map(result -> envelope.createUnion(new Envelope2D(result))).orElse(envelope));
    }

    @Override
    public Matrix transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
        double srcX = srcPts[srcOff + 0];
        double srcY = srcPts[srcOff + 1];

        try {
            double[] result = this.fromGeo ? this.projection.fromGeo(srcX, srcY) : this.projection.toGeo(srcX, srcY);

            dstPts[dstOff + 0] = result[0];
            dstPts[dstOff + 1] = result[1];

            if (!derivate) {
                return null;
            }

            throw new UnsupportedOperationException();
        } catch (OutOfProjectionBoundsException e) {
            throw new ProjectionException(e);
        }
    }

    @Override
    public MathTransform inverse() {
        return new DymaxionProjectionMathTransform(!this.fromGeo);
    }
}
