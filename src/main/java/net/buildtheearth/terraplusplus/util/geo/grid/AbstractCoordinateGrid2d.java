package net.buildtheearth.terraplusplus.util.geo.grid;

import lombok.Getter;
import lombok.NonNull;
import net.daporkchop.lib.common.annotation.param.NotNegative;
import net.daporkchop.lib.common.annotation.param.Positive;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Getter
public abstract class AbstractCoordinateGrid2d extends AbstractCoordinateGrid implements CoordinateGrid2d {
    private final int sizeX;
    private final int sizeY;

    public AbstractCoordinateGrid2d(CoordinateGrid parent, @NonNull CoordinateReferenceSystem crs, @NotNegative int sizeX, @NotNegative int sizeY) {
        super(parent, crs);

        this.sizeX = notNegative(sizeX, "sizeX");
        this.sizeY = notNegative(sizeY, "sizeY");

        //make sure that nothing will overflow:
        //noinspection ResultOfMethodCallIgnored
        Math.multiplyExact(Math.multiplyExact(sizeX, sizeY), 2);
    }

    @Override
    public final @Positive int dimensions() {
        return 2;
    }

    @Override
    public @NotNegative int[] sizes() {
        return new @NotNegative int[]{ this.sizeX(), this.sizeY() };
    }

    @Override
    public @NotNegative int totalSize() {
        return Math.multiplyExact(this.sizeX(), this.sizeY());
    }

    @Override
    public @NotNegative int totalValuesSize() {
        return Math.multiplyExact(this.totalSize(), 2);
    }

    @Override
    public int points(@NonNull double[] dst, @NotNegative int dstOff) {
        int totalValues = this.totalValuesSize();
        checkRangeLen(dst.length, dstOff, totalValues);

        int sizeX = this.sizeX();
        int sizeY = this.sizeY();

        double[] buf = new double[2];

        for (int writerIndex = dstOff, x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++, writerIndex += 2) {
                this.point(x, y, buf);
                System.arraycopy(buf, 0, dst, writerIndex, 2);
            }
        }

        return totalValues;
    }
}
