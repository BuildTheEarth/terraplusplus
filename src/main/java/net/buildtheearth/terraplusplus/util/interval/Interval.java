package net.buildtheearth.terraplusplus.util.interval;

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
     * @return the length of this interval
     */
    default double length() {
        return this.max() - this.min();
    }

    /**
     * Checks whether or not this interval contains the given point.
     *
     * @param point the point
     * @return whether or not this interval contains the given point
     */
    default boolean contains(double point) {
        return this.min() < point && this.max() > point;
    }
}
