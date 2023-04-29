package net.buildtheearth.terraplusplus.util.geo.grid;

import lombok.NonNull;
import net.daporkchop.lib.common.annotation.param.NotNegative;
import net.daporkchop.lib.common.annotation.param.Positive;
import org.apache.sis.geometry.Envelope2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public final class AxisAlignedCoordinateGrid2d extends AbstractCoordinateGrid2d {
    private final double x;
    private final double y;
    private final double w;
    private final double h;

    private final double indexScaleX;
    private final double indexScaleY;

    public AxisAlignedCoordinateGrid2d(CoordinateGrid parent, @NonNull CoordinateReferenceSystem crs, @Positive int sizeX, @Positive int sizeY, double x, double y, double w, double h) {
        super(parent, crs, sizeX, sizeY);

        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        this.indexScaleX = w / sizeX;
        this.indexScaleY = h / sizeY;
    }

    @Override
    public double[] point(@NotNegative int x, @NotNegative int y, double[] dst) {
        checkIndex(this.sizeX(), x);
        checkIndex(this.sizeY(), y);

        if (dst != null) {
            checkArg(dst.length == 2, dst.length);
        } else {
            dst = new double[2];
        }

        dst[0] = this.x + x * this.indexScaleX;
        dst[1] = this.y + y * this.indexScaleY;

        return dst;
    }

    @Override
    public Envelope2D envelope() {
        return new Envelope2D(this.crs(), this.x, this.y, this.w, this.h);
    }
}
