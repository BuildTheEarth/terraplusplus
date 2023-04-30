package net.buildtheearth.terraplusplus.util.geo.pointarray;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.daporkchop.lib.common.annotation.param.NotNegative;
import net.daporkchop.lib.common.annotation.param.Positive;
import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.operation.transform.LinearTransform;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.util.FactoryException;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public final class AxisAlignedGridPointArray2D extends AbstractPointArray2D {
    private final int sizeX;
    private final int sizeY;

    private final double x;
    private final double y;
    private final double w;
    private final double h;

    private final double indexScaleX;
    private final double indexScaleY;

    public AxisAlignedGridPointArray2D(PointArray parent, @NonNull CoordinateReferenceSystem crs, @Positive int sizeX, @Positive int sizeY, double x, double y, double w, double h) {
        super(parent, crs, Math.multiplyExact(sizeX, sizeY));

        this.sizeX = sizeX;
        this.sizeY = sizeY;

        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        this.indexScaleX = w / sizeX;
        this.indexScaleY = h / sizeY;
    }

    @Override
    public double[] point(@NotNegative int index, double[] dst) {
        checkIndex(this.size(), index);

        if (dst != null) {
            checkArg(dst.length == 2, dst.length);
        } else {
            dst = new double[2];
        }

        int x = index / this.sizeY;
        int y = index % this.sizeY;

        dst[0] = this.x + x * this.indexScaleX;
        dst[1] = this.y + y * this.indexScaleY;

        return dst;
    }

    @Override
    public int points(@NonNull double[] dst, @NotNegative int dstOff) {
        int totalValues = this.coordinatesSize();
        checkRangeLen(dst.length, dstOff, totalValues);

        for (int writerIndex = dstOff, x = 0; x < this.sizeX; x++) {
            for (int y = 0; y < this.sizeY; y++, writerIndex++) {
                dst[dstOff + 0] = this.x + x * this.indexScaleX;
                dst[dstOff + 1] = this.y + y * this.indexScaleY;
            }
        }

        return totalValues;
    }

    @Override
    public Envelope2D envelope() {
        return new Envelope2D(this.crs(), this.x, this.y, this.w, this.h);
    }

    @Override
    @SneakyThrows(FactoryException.class)
    public PointArray convert(@NonNull CoordinateReferenceSystem crs, double maxError) {
        if (this.crs().equals(crs)) {
            return this;
        }

        //TODO: i don't necessarily want to enforce this
        checkArg(crs.getCoordinateSystem().getDimension() == this.pointDimensions());

        CoordinateOperation operation = CRS.findOperation(this.crs(), crs, null);
        MathTransform transform = operation.getMathTransform();
        if (transform.isIdentity()) {
            return this;
        } else if (transform instanceof LinearTransform) {
            //TODO: turn into a corner bounding box
        }

        return new TransformedGridPointArray2D(this, crs, operation);
    }
}
