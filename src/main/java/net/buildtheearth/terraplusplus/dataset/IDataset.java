package net.buildtheearth.terraplusplus.dataset;

import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
public interface IDataset<K, V> {
    CompletableFuture<V> getAsync(@NonNull K key);
}
