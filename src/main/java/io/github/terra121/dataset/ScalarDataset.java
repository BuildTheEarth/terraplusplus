package io.github.terra121.dataset;

import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * A dataset consisting of floating-point scalar values.
 *
 * @author DaPorkchop_
 */
public interface ScalarDataset {
    /**
     * Gets the value at the given coordinates.
     *
     * @param lon the longitude of the value to get
     * @param lat the latitude of the value to get
     * @return the value
     */
    double get(double lon, double lat);

    /**
     * Asynchronously gets a bunch of values at the given coordinates.
     *
     * @param lons the longitudes of the values to get
     * @param lats the latitudes of the values to get
     * @return a {@link CompletableFuture} which will be completed with the values
     */
    CompletableFuture<double[]> getAsync(@NonNull double[] lons, @NonNull double[] lats);
}
