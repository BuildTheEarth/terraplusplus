package io.github.terra121.util.bvh;

import lombok.NonNull;

import static java.lang.Math.*;

/**
 * An axis-aligned, 2D bounding box using double-precision floating-point coordinates.
 *
 * @author DaPorkchop_
 */
public interface Bounds2d {
    static Bounds2d of(double x0, double x1, double z0, double z1) {
        return new Bounds2dImpl(min(x0, x1), max(x0, x1), min(z0, z1), max(z0, z1));
    }

    /**
     * @return the minimum X coordinate
     */
    double minX();

    /**
     * @return the maximum X coordinate
     */
    double maxX();

    /**
     * @return the minimum Z coordinate
     */
    double minZ();

    /**
     * @return the maximum Z coordinate
     */
    double maxZ();

    /**
     * Checks whether or not this bounding box intersects the given bounding box.
     *
     * @param other the bounding box
     * @return whether or not the two bounding boxes intersect
     */
    default boolean intersects(@NonNull Bounds2d other) {
        return this.minX() < other.maxX() && this.maxX() > other.minX() && this.minZ() < other.maxZ() && this.maxZ() > other.minZ();
    }

    /**
     * Checks whether or not this bounding box contains the given bounding box.
     *
     * @param other the bounding box
     * @return whether or not this bounding box contains the given bounding box
     */
    default boolean contains(@NonNull Bounds2d other) {
        return this.minX() <= other.minX() && this.maxX() >= other.maxX() && this.minZ() <= other.minX() && this.maxZ() >= other.maxX();
    }
}
