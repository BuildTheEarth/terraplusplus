package net.buildtheearth.terraplusplus.dataset;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.geo.pointarray.PointArray2D;

import java.util.concurrent.CompletableFuture;

/**
 * A dataset consisting of floating-point scalar values.
 *
 * @author DaPorkchop_
 */
public interface IScalarDataset {
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
     * Asynchronously gets a bunch of values at the given coordinates.
     *
     * @param points an array of points
     * @return a {@link CompletableFuture} which will be completed with the values
     */
    CompletableFuture<double[]> getAsync(@NonNull PointArray2D points) throws OutOfProjectionBoundsException;

    /**
     * @return the number of meters between sample points (in geographic/unprojected coordinate space)
     */
    default double[] degreesPerSample() {
        throw new UnsupportedOperationException();
    }
}
