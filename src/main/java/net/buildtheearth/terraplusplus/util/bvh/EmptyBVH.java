package net.buildtheearth.terraplusplus.util.bvh;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * Implementation of {@link BVH} which contains no values.
 *
 * @author DaPorkchop_
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class EmptyBVH<V extends Bounds2d> implements BVH<V> {
    private static final EmptyBVH INSTANCE = new EmptyBVH();

    public static <V extends Bounds2d> EmptyBVH<V> get() {
        return uncheckedCast(INSTANCE);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public List<V> getAllIntersecting(@NonNull Bounds2d bb) {
        return Collections.emptyList();
    }

    @Override
    public void forEachIntersecting(@NonNull Bounds2d bb, @NonNull Consumer<V> callback) {
        //no-op
    }

    @Override
    public Stream<V> stream() {
        return Stream.empty();
    }

    @Override
    public double minX() {
        return 0.0d;
    }

    @Override
    public double maxX() {
        return 0.0d;
    }

    @Override
    public double minZ() {
        return 0.0d;
    }

    @Override
    public double maxZ() {
        return 0.0d;
    }

    @Override
    public Iterator<V> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public void forEach(@NonNull Consumer<? super V> action) {
        //no-op
    }

    @Override
    public Spliterator<V> spliterator() {
        return Spliterators.emptySpliterator();
    }
}
