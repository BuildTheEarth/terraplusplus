package net.buildtheearth.terraplusplus.dataset;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.scalar.ConfigurableDoubleTiledDataset;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;

import java.util.concurrent.CompletableFuture;

/**
 * A dataset consisting of floating-point scalar values.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize(as = ConfigurableDoubleTiledDataset.class)
public interface IScalarDataset {
    /**
     * @param point the point
     * @see #getAsync(double, double)
     */
    default CompletableFuture<Double> getAsync(@NonNull double[] point) throws OutOfProjectionBoundsException {
        return this.getAsync(point[0], point[1]);
    }

    /**
     * Asynchronously gets a single value at the given point.
     *
     * @param lon the longitude
     * @param lat the latitude
     * @return a {@link CompletableFuture} which will be completed with the value
     */
    CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException;

    /**
     * Asynchronously gets a bunch of values at the given coordinates.
     *
     * @param sizeX the number of samples to take along the X axis
     * @param sizeZ the number of samples to take along the Z axis
     * @return a {@link CompletableFuture} which will be completed with the values
     */
    CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException;

    /**
     * @return the number of raw (unscaled) sample points contained in the given bounding box
     */
    default double sampleCountIn(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException {
        throw new UnsupportedOperationException(this.getClass().getCanonicalName());
    }
}
