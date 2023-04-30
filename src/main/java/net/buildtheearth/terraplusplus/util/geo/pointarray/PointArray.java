package net.buildtheearth.terraplusplus.util.geo.pointarray;

import lombok.NonNull;
import net.daporkchop.lib.common.annotation.param.NotNegative;
import net.daporkchop.lib.common.annotation.param.Positive;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * An array of points which can be converted between coordinate systems.
 *
 * @author DaPorkchop_
 */
public interface PointArray {
    /**
     * @return the dimensionality of each point in this array
     */
    @Positive int pointDimensions();

    /**
     * @return the length of this array
     */
    @NotNegative int size();

    /**
     * @return the total number of coordinate values in this array, equal to the number of points times the {@link #pointDimensions() point dimensionality}
     */
    default @NotNegative int totalValueSize() {
        return Math.multiplyExact(this.size(), this.pointDimensions());
    }

    /**
     * Gets the coordinates of the point at the given index.
     *
     * @param index the index of the point to get
     * @param dst   a {@code double[]} to store the resulting point coordinates in. May be {@code null}, in which case a new array will be allocated and returned.
     * @return the coordinates of the point at the given index
     * @throws IndexOutOfBoundsException if the given index exceeds this array's size
     * @throws IllegalArgumentException  if the given destination array is non-{@code null} and its length is not equal to this array's {@link #pointDimensions() point dimensionality}
     */
    double[] point(@NotNegative int index, double[] dst);

    /**
     * Gets the coordinates of every pont in this array.
     * <p>
     * Points are written tightly packed into the given destination array in index order.
     *
     * @param dst    a {@code double[]} to store the resulting point coordinates in
     * @param dstOff the starting index to begin writing into the given destination array
     * @return the number of {@code double} values written into the given array, equal to {@link #totalValueSize()}
     * @throws IndexOutOfBoundsException if the given destination array has insufficient capacity for the coordinate values (i.e. less than {@link #totalValueSize()})
     */
    int points(@NonNull double[] dst, @NotNegative int dstOff);

    /**
     * @return the {@link CoordinateReferenceSystem CRS} in which the array's points are represented
     */
    CoordinateReferenceSystem crs();

    /**
     * @return an {@link Envelope} which contains all points in this array
     */
    Envelope envelope();

    /**
     * @return the point array from which this point array is derived, or {@code null} if this is not a derived point array
     */
    PointArray parent();

    /**
     * Converts the points in this array to the given {@link CoordinateReferenceSystem coordinate system}.
     * <p>
     * Any points in this array which are unable to converted to the given coordinate system will have all their coordinates set to {@link Double#NaN}, and will
     * remain invalid even in the case of additional subsequent conversions.
     *
     * @param crs      the {@link CoordinateReferenceSystem coordinate system} to transform this array's points into
     * @param maxError indicates the maximum permitted error (the distance between the points in the returned {@link PointArray} and the point's actual positions
     *                 if they were transformed individually to the target coordinate system). This may be used to permit an implementation to optimize a
     *                 transformation at the cost of some accuracy, while setting an upper bound on the potential loss of precision.
     * @return a {@link PointArray} containing this array's points after conversion to the given coordinate system
     */
    PointArray convert(@NonNull CoordinateReferenceSystem crs, double maxError);

    /**
     * @return a {@code double[]} with each element indicating the estimated expected number of units between samples along the corresponding axis
     */
    double[] estimatedPointDensity();
}
