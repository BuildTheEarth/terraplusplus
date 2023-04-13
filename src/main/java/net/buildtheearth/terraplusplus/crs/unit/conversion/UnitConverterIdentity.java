package net.buildtheearth.terraplusplus.crs.unit.conversion;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.unit.UnitConverter;

/**
 * An {@link UnitConverter} which does nothing.
 *
 * @author DaPorkchop_
 */
public final class UnitConverterIdentity implements UnitConverter {
    private static final UnitConverterIdentity INSTANCE = new UnitConverterIdentity();

    public static UnitConverterIdentity instance() {
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
    public UnitConverter inverse() {
        return this;
    }

    @Override
    public UnitConverter simplify() {
        return this;
    }

    @Override
    public UnitConverter andThen(@NonNull UnitConverter next) {
        return next;
    }

    @Override
    public UnitConverter intern() {
        return this;
    }
}
