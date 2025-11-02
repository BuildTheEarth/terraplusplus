package net.buildtheearth.terraminusminus.dataset;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraminusminus.util.http.Http;

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
        return Http.getFirst(
                Arrays.stream(this.urls()).map(s -> s + key).toArray(String[]::new),
                Http.RequestOptions.builder().build(),
                data -> this.decode(key, data));
    }
}
