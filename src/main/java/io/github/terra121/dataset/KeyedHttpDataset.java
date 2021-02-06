package io.github.terra121.dataset;

import io.github.terra121.util.http.Http;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public abstract class KeyedHttpDataset<V> extends Dataset<String, V> {
    @NonNull
    protected final String[] urls;

    protected abstract V decode(@NonNull String path, @NonNull ByteBuf data) throws Exception;

    @Override
    public CompletableFuture<V> load(@NonNull String key) throws Exception {
        return Http.getFirst(this.urls(), data -> this.decode(key, data));
    }
}
