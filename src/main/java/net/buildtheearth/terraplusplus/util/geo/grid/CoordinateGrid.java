package net.buildtheearth.terraplusplus.util.geo.grid;

import lombok.NonNull;
import net.daporkchop.lib.common.annotation.param.NotNegative;
import net.daporkchop.lib.common.annotation.param.Positive;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A grid of geographic coordinates which can be converted between coordinate systems.
 * <p>
 * In the original coordinate system, the grid's coordinates are axis-aligned, however after conversion to another coordinate system
 * this may no longer be the case.
 *
 * @author DaPorkchop_
 */
public interface CoordinateGrid {
    /**
     * @return the dimensionality of this grid
     */
    @Positive int dimensions();

    /**
     * Gets the number of points along the given dimension.
     *
     * @param dim the dimension number
     * @return the number of points along the given dimension
     */
    @NotNegative int size(@NotNegative int dim);

    /**
     * @return an array whose length is equal to this grid's {@link #dimensions() dimensionality}, where each element indicates the number of points along the corresponding dimension
     */
    default @NotNegative int[] sizes() {
        int dimensions = this.dimensions();
        int[] sizes = new int[dimensions];
        for (int dim = 0; dim < dimensions; dim++) {
            sizes[dim] = this.size(dim);
        }
        return sizes;
    }

    /**
     * @return the total number of points in this grid
     */
    default @NotNegative int totalSize() {
        int product = 1;
        for (int dim = 0, dimensions = this.dimensions(); dim < dimensions; dim++) {
            product = Math.multiplyExact(this.size(dim), product);
        }
        return product;
    }

    /**
     * @return the total number of coordinate values in this grid, equal to the number of points times the {@link #dimensions() dimensionality}
     */
    default @NotNegative int totalValuesSize() {
        return Math.multiplyExact(this.totalSize(), this.dimensions());
    }

    /**
     * Gets the coordinates of the point at the given index.
     *
     * @param index the point index along each axis
     * @param dst   a {@code double[]} to store the resulting point coordinates in. May be {@code null}, in which case a new array will be allocated and returned.
     * @return the coordinates of the point at the given index
     * @throws IllegalArgumentException  if the given index array's length is not equal to this grid's {@link #dimensions() dimensionality}
     * @throws IllegalArgumentException  if the given destination array is non-{@code null} and its length is not equal to this grid's {@link #dimensions() dimensionality}
     * @throws IndexOutOfBoundsException if any of the given indices exceed this grid's size along the corresponding dimension
     */
    double[] point(@NonNull int[] index, double[] dst);

    /**
     * Gets the coordinates of every pont in this grid.
     * <p>
     * Points are written tightly packed into the given destination array, with the iteration order being such that dimension with the lowest index is incremented last.
     *
     * @param dst    a {@code double[]} to store the resulting point coordinates in
     * @param dstOff the starting index to begin writing into the given destination array
     * @return the number of {@code double} values written into the given array, equal to {@link #totalValuesSize()}
     * @throws IndexOutOfBoundsException if the given destination array has insufficient capacity for the coordinate values (i.e. less than {@link #totalValuesSize()})
     */
    int points(@NonNull double[] dst, @NotNegative int dstOff);

    /**
     * @return the {@link CoordinateReferenceSystem CRS} in which the grid's points are represented
     */
    CoordinateReferenceSystem crs();

    /**
     * @return an {@link Envelope} which contains all points in this grid
     */
    Envelope envelope();

    /**
     * @return the coordinate grid from which this coordinate grid is derived, or {@code null} if this is not a derived coordinate grid
     */
    CoordinateGrid parent();

    /**
     * Converts this grid to the given {@link CoordinateReferenceSystem coordinate system}.
     * <p>
     * Any points in this grid which are unable to converted to the given coordinate system will have all their coordinates set to {@link Double#NaN}, and will
     * remain invalid even in the case of additional subsequent conversions.
     *
     * @return a {@link CoordinateGrid} containing this grid's points after conversion to the given coordinate system
     */
    CoordinateGrid convert(@NonNull CoordinateReferenceSystem crs);
}
