package net.buildtheearth.terraplusplus.util.geo.pointarray;

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
public abstract class AbstractPointArray2D extends AbstractPointArray implements PointArray2D {
    public AbstractPointArray2D(PointArray parent, @NonNull CoordinateReferenceSystem crs, @NotNegative int size) {
        super(parent, crs, size);

        int crsDimension = crs.getCoordinateSystem().getDimension();
        checkArg(crsDimension == 2, "coordinate system has %d dimensions: %s", crsDimension, crs);

        //make sure that nothing will overflow:
        //noinspection ResultOfMethodCallIgnored
        Math.multiplyExact(size, 2);
    }

    @Override
    public final @Positive int pointDimensions() {
        return 2;
    }

    @Override
    public @NotNegative int coordinatesSize() {
        return Math.multiplyExact(this.size(), 2);
    }

    @Override
    public int points(@NonNull double[] dst, @NotNegative int dstOff) {
        int totalValues = this.coordinatesSize();
        checkRangeLen(dst.length, dstOff, totalValues);

        double[] buf = new double[2];
        for (int writerIndex = dstOff, i = 0, size = this.size(); i < size; i++, writerIndex += 2) {
            this.point(i, buf);
            System.arraycopy(buf, 0, dst, writerIndex, 2);
        }

        return totalValues;
    }
}
