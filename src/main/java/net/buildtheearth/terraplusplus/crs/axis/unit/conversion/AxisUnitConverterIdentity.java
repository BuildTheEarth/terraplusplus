package net.buildtheearth.terraplusplus.crs.axis.unit.conversion;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnitConverter;

/**
 * An {@link AxisUnitConverter} which does nothing.
 *
 * @author DaPorkchop_
 */
public final class AxisUnitConverterIdentity implements AxisUnitConverter {
    private static final AxisUnitConverterIdentity INSTANCE = new AxisUnitConverterIdentity();

    public static AxisUnitConverterIdentity instance() {
        return INSTANCE;
    }

    @Override
    public boolean isIdentity() {
        return true;
    }

    @Override
    public double convert(double value) {
        return value;
    }

    @Override
    public void convert(@NonNull double[] src, int srcOff, @NonNull double[] dst, int dstOff, int cnt) {
        //noinspection ArrayEquality
        if (src != dst || srcOff != dstOff) {
            System.arraycopy(src, srcOff, dst, dstOff, cnt);
        }
    }

    @Override
    public AxisUnitConverter inverse() {
        return this;
    }

    @Override
    public AxisUnitConverter simplify() {
        return this;
    }

    @Override
    public AxisUnitConverter andThen(@NonNull AxisUnitConverter next) {
        return next;
    }

    @Override
    public AxisUnitConverter intern() {
        return this;
    }
}
