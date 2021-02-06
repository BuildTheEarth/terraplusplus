package io.github.terra121.util.bvh;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Implementation of {@link BVH} which contains a single value.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
final class SingletonBVH<V extends Bounds2d> implements BVH<V> {
    @NonNull
    protected final V value;

    @Override
    public int size() {
        return 1;
    }

    @Override
    public List<V> getAllIntersecting(@NonNull Bounds2d bb) {
        return bb.intersects(this.value) ? Lists.newArrayList(this.value) : Collections.emptyList();
    }

    @Override
    public void forEachIntersecting(@NonNull Bounds2d bb, @NonNull Consumer<V> callback) {
        if (bb.intersects(this.value)) {
            callback.accept(this.value);
        }
    }

    @Override
    public Stream<V> stream() {
        return Stream.of(this.value);
    }

    @Override
    public double minX() {
        return this.value.minX();
    }

    @Override
    public double maxX() {
        return this.value.maxX();
    }

    @Override
    public double minZ() {
        return this.value.minZ();
    }

    @Override
    public double maxZ() {
        return this.value.maxZ();
    }

    @Override
    public Iterator<V> iterator() {
        return Iterators.singletonIterator(this.value);
    }

    @Override
    public void forEach(@NonNull Consumer<? super V> action) {
        action.accept(this.value);
    }

    @Override
    public Spliterator<V> spliterator() {
        return this.stream().spliterator();
    }
}
