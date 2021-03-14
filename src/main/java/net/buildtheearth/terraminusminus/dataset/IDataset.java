package net.buildtheearth.terraminusminus.dataset;

import java.util.concurrent.CompletableFuture;

import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
public interface IDataset<K, V> {
    CompletableFuture<V> getAsync(@NonNull K key);
}
