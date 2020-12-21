package io.github.terra121.util.interval;

import lombok.NonNull;

/**
 * A 1-dimensional interval represented using double-precision floating-point coordinates.
 *
 * @author DaPorkchop_
 */
public interface Interval {
    static Interval of(double v0, double v1) {
        return new IntervalImpl(Math.min(v0, v1), Math.max(v0, v1));
    }

    /**
     * @return the minimum coordinate
     */
    double min();

    /**
     * @return the maximum coordinate
     */
    double max();

    /**
     * Checks whether or not this interval intersects the given interval.
     *
     * @param other the interval
     * @return whether or not the two intervales intersect
     */
    default boolean intersects(@NonNull Interval other) {
        return this.min() < other.max() && this.max() > other.min();
    }

    /**
     * Checks whether or not this interval contains the given interval.
     *
     * @param other the interval
     * @return whether or not this interval contains the given interval
     */
    default boolean contains(@NonNull Interval other) {
        return this.min() <= other.min() && this.max() >= other.max();
    }
}
