package io.github.terra121.util.bvh;

import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A simple, immutable, quadtree-based implementation of a BVH (Bounding Volume Hierarchy) on arbitrary values implementing {@link Bounds2d}.
 *
 * @author DaPorkchop_
 */
public interface BVH<V extends Bounds2d> extends Bounds2d, Iterable<V> {
    static <V extends Bounds2d> BVH<V> of(@NonNull V[] values) {
        if (values.length == 0) {
            return EmptyBVH.get();
        } else if (values.length == 1) {
            return new SingletonBVH<>(values[0]);
        } else {
            return new QuadtreeBVH<>(values);
        }
    }

    /**
     * @return the number of values
     */
    int size();

    /**
     * Gets a {@link List} containing every value that intersects with the given bounding box.
     *
     * @param bb the bounding box that values must intersect with
     * @return the values
     */
    List<V> getAllIntersecting(@NonNull Bounds2d bb);

    /**
     * Runs the given function on every value that intersects with the given bounding box.
     *
     * @param bb       the bounding box that values must intersect with
     * @param callback the callback function to run
     */
    void forEachIntersecting(@NonNull Bounds2d bb, @NonNull Consumer<V> callback);

    /**
     * @see Collection#stream()
     */
    Stream<V> stream();
}
