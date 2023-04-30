package net.buildtheearth.terraplusplus.util.geo.pointarray;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.util.compat.sis.SISHelper;
import net.daporkchop.lib.common.annotation.param.NotNegative;
import org.apache.sis.geometry.Envelope2D;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public final class TransformedGridPointArray2D extends AbstractPointArray2D {
    private final CoordinateOperation operation;
    private final MathTransform transform;

    public TransformedGridPointArray2D(@NonNull PointArray parent, @NonNull CoordinateReferenceSystem crs) {
        this(parent, crs, SISHelper.findOperation(parent.crs(), crs));
    }

    TransformedGridPointArray2D(@NonNull PointArray parent, @NonNull CoordinateReferenceSystem crs, @NonNull CoordinateOperation operation) {
        super(parent, crs, parent.size());

        checkArg(parent.pointDimensions() == this.pointDimensions()); //TODO: maybe don't require this in the future?

        checkArg(operation.getSourceCRS().equals(parent.crs()), "parent crs=%s, operation source crs=%s", parent.crs(), operation.getSourceCRS());
        checkArg(operation.getTargetCRS().equals(crs), "this crs=%s, operation target crs=%s", crs, operation.getTargetCRS());

        this.operation = operation;
        this.transform = operation.getMathTransform();
    }

    @Override
    public double[] point(@NotNegative int index, double[] dst) {
        dst = this.parent().point(index, dst);
        SISHelper.transformSinglePointWithOutOfBoundsNaN(this.transform, dst, 0, dst, 0);
        return dst;
    }

    @Override
    public int points(@NonNull double[] dst, @NotNegative int dstOff) {
        this.parent().points(dst, dstOff);
        SISHelper.transformManyPointsWithOutOfBoundsNaN(this.transform, dst, dstOff, dst, dstOff, this.size());
        return this.totalValueSize();
    }

    @Override
    @SneakyThrows(TransformException.class)
    public Envelope2D envelope() {
        return new Envelope2D(SISHelper.transform(this.operation, this.parent().envelope()));
    }

    @Override
    public PointArray convert(@NonNull CoordinateReferenceSystem crs, double maxError) {
        if (this.crs().equals(crs)) {
            return this;
        }

        //TODO: this could be optimized quite a bit
        return new TransformedGridPointArray2D(this, crs);
    }

    @Override
    public double[] estimatedPointDensity() {
        double[] parentDensity = this.parent().estimatedPointDensity();
        Envelope parentEnvelope = this.parent().envelope();
        Envelope selfEnvelope = this.envelope();

        //TODO: this assumes axis order is consistent between the two!

        return new double[]{
                parentDensity[0] * parentEnvelope.getSpan(0) / selfEnvelope.getSpan(0),
                parentDensity[1] * parentEnvelope.getSpan(1) / selfEnvelope.getSpan(1),
        };
    }
}
