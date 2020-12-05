package io.github.terra121.dataset;

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
    double estimateLocal(double lon, double lat);
}
