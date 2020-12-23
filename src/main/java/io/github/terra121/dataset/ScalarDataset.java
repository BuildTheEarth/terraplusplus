package io.github.terra121.dataset;

import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * A dataset consisting of floating-point scalar values.
 *
 * @author DaPorkchop_
 */
public interface ScalarDataset {
    /**
     * @deprecated use {@link #getAsync(CornerBoundingBox2d, int, int)}
     */
    @Deprecated
    default double get(double lon, double lat) {
        throw new UnsupportedOperationException();
    }

    /**
     * Asynchronously gets a bunch of values at the given coordinates.
     *
     * @param sizeX the number of samples to take along the X axis
     * @param sizeZ the number of samples to take along the Z axis
     * @return a {@link CompletableFuture} which will be completed with the values
     */
    CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException;
}
