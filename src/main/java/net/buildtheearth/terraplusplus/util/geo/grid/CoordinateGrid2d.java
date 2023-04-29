package net.buildtheearth.terraplusplus.util.geo.grid;

import lombok.NonNull;
import net.daporkchop.lib.common.annotation.param.NotNegative;
import net.daporkchop.lib.common.annotation.param.Positive;
import org.apache.sis.geometry.Envelope2D;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Specialization of {@link CoordinateGrid} for two dimensions.
 *
 * @author DaPorkchop_
 */
public interface CoordinateGrid2d extends CoordinateGrid {
    @Override
    default @Positive int dimensions() {
        return 2;
    }

    /**
     * @deprecated use {@link #sizeX()} and {@link #sizeY()} respectively
     */
    @Override
    @Deprecated
    default @NotNegative int size(@NotNegative int dim) {
        switch (dim) {
            case 0:
                return this.sizeX();
            case 1:
                return this.sizeY();
            default:
                throw new IndexOutOfBoundsException(String.valueOf(dim));
        }
    }

    /**
     * @return this grid's size along the X axis
     */
    @NotNegative int sizeX();

    /**
     * @return this grid's size along the Y axis
     */
    @NotNegative int sizeY();

    @Override
    default @NotNegative int totalSize() {
        return Math.multiplyExact(this.sizeX(), this.sizeY());
    }

    @Override
    default @NotNegative int totalValuesSize() {
        return Math.multiplyExact(this.totalSize(), 2);
    }

    /**
     * @deprecated use {@link #point(int, int, double[])}
     */
    @Override
    @Deprecated
    default double[] point(@NonNull int[] index, double[] dst) {
        checkArg(index.length == 2, index.length);
        return this.point(index[0], index[1], dst);
    }

    /**
     * Gets the coordinates of the point at the given index.
     *
     * @param x   the index along the X axis
     * @param y   the index along the Y axis
     * @param dst a {@code double[]} to store the resulting point coordinates in. May be {@code null}, in which case a new array will be allocated and returned.
     * @return the coordinates of the point at the given index
     * @throws IllegalArgumentException  if the given destination array is non-{@code null} and its length is not equal to this grid's {@link #dimensions() dimensionality}
     * @throws IndexOutOfBoundsException if either the given {@code x} or {@code y} indices exceed this grid's size along the corresponding dimension
     */
    double[] point(@NotNegative int x, @NotNegative int y, double[] dst);

    @Override
    Envelope2D envelope();
}
