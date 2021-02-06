package io.github.terra121.dataset;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author DaPorkchop_
 */
public abstract class Dataset<K, V> extends CacheLoader<K, CompletableFuture<V>> implements IDataset<K, V> {
    protected final LoadingCache<K, CompletableFuture<V>> cache = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .build(this);

    @Override
    public CompletableFuture<V> getAsync(@NonNull K key) {
        return this.cache.getUnchecked(key);
    }

    /**
     * @deprecated internal API, don't call this method directly!
     */
    @Override
    @Deprecated
    public abstract CompletableFuture<V> load(@NonNull K key) throws Exception;
}
