package net.buildtheearth.terraplusplus.crs.unit;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.util.Internable;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public interface UnitConverter extends Internable<UnitConverter> {
    boolean isIdentity();

    /**
     * Converts the given value.
     *
     * @param value the value to convert
     * @return the converted value
     */
    double convert(double value);

    /**
     * Converts the values stored in the given range of the given source array, and stores the results in the given range of the given destination array.
     * <p>
     * {@code src} and {@code dst} may refer to the same array. However, if they refer to overlapping regions of the same array, the behavior is only defined if the source and destination ranges are identical (in which
     * case the values are converted in place).
     *
     * @param src    the array containing the source values
     * @param srcOff the index of the first element in the source array
     * @param dst    the array containing the destination values
     * @param dstOff the index of the first element in the destination array
     * @param cnt    the number of elements to convert
     * @throws IndexOutOfBoundsException if the given index ranges exceed the bounds of the source or destination arrays. In this case, it is undefined whether any values were written to the destination array.
     */
    default void convert(@NonNull double[] src, int srcOff, @NonNull double[] dst, int dstOff, int cnt) {
        notNegative(cnt, "cnt");
        checkRangeLen(src.length, srcOff, cnt);
        checkRangeLen(dst.length, dstOff, cnt);

        for (int i = 0; i < cnt; i++) {
            dst[dstOff + i] = this.convert(src[srcOff + i]);
        }
    }

    /**
     * Converts the values stored in the given range of the given source array, and stores the results in the given range of the given destination array.
     * <p>
     * {@code src} and {@code dst} may refer to the same array. However, if they refer to overlapping regions of the same array, the behavior is only defined if the source and destination ranges are identical (in which
     * case the values are converted in place).
     *
     * @param src       the array containing the source values
     * @param srcOff    the index of the first element in the source array
     * @param srcStride the spacing between elements in the source array. Must be positive
     * @param dst       the array containing the destination values
     * @param dstOff    the index of the first element in the destination array
     * @param dstStride the spacing between elements in the destination array. Must be positive
     * @param cnt       the number of elements to convert
     * @throws IndexOutOfBoundsException if the given index ranges exceed the bounds of the source or destination arrays. In this case, it is undefined whether any values were written to the destination array.
     */
    default void convert(@NonNull double[] src, int srcOff, int srcStride, @NonNull double[] dst, int dstOff, int dstStride, int cnt) {
        notNegative(cnt, "cnt");
        positive(srcStride, "srcStride");
        positive(dstStride, "dstStride");

        if (srcStride == 1 && dstStride == 1) { //elements are tightly packed
            this.convert(src, srcOff, dst, dstOff, cnt);
        }

        //TODO: pre-validate array bounds

        for (int i = 0; i < cnt; i++, srcOff += srcStride, dstOff += dstStride) {
            dst[dstOff] = this.convert(src[srcOff]);
        }
    }

    /**
     * @return an {@link UnitConverter} which performs the inverse of this converter's operation
     */
    UnitConverter inverse();

    /**
     * @return an {@link UnitConverter} which is equivalent to this one, but may be able to execute more efficiently
     */
    UnitConverter simplify();

    /**
     * Prefixes this {@link UnitConverter} with the given {@link UnitConverter}, resulting in an {@link UnitConverter} such that
     * {@code next.convert(this.convert(x)) == this.andThen(next).convert(x)} (ignoring any potential loss of precision).
     *
     * @param next the next {@link UnitConverter}
     * @return the resulting concatenated {@link UnitConverter}
     */
    UnitConverter andThen(@NonNull UnitConverter next);
}
