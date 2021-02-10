package io.github.terra121.dataset;

import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
public interface IDataset<K, V> {
    CompletableFuture<V> getAsync(@NonNull K key);
}
