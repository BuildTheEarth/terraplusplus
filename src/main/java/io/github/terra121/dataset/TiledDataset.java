package io.github.terra121.dataset;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.http.Http;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.misc.string.PStrings;
import net.minecraft.util.math.ChunkPos;

@RequiredArgsConstructor
@Getter
public abstract class TiledDataset<T> extends CacheLoader<ChunkPos, CompletableFuture<T>> {
    protected final LoadingCache<ChunkPos, CompletableFuture<T>> cache = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .build(this);

    @NonNull
    protected final GeographicProjection projection;

    protected final double tileSize;

    protected abstract String[] urls(int tileX, int tileZ);

    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        builder.put("x", String.valueOf(tileX))
                .put("z", String.valueOf(tileZ))
                .put("lon.min", PStrings.fastFormat("%.12f", tileX * this.tileSize))
                .put("lon.max", PStrings.fastFormat("%.12f", (tileX + 1) * this.tileSize))
                .put("lat.min", PStrings.fastFormat("%.12f", tileZ * this.tileSize))
                .put("lat.max", PStrings.fastFormat("%.12f", (tileZ + 1) * this.tileSize));
    }

    protected abstract T decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception;

    @Deprecated
    public T getTile(int tileX, int tileZ) {
        return this.getTileAsync(tileX, tileZ).join();
    }

    public CompletableFuture<T> getTileAsync(int tileX, int tileZ) {
        return this.getTileAsync(new ChunkPos(tileX, tileZ));
    }

    public CompletableFuture<T> getTileAsync(@NonNull ChunkPos pos) {
        return this.cache.getUnchecked(pos);
    }

    /**
     * internal API, don't call this method directly!
     */
    @Deprecated
    @Override
    public CompletableFuture<T> load(@NonNull ChunkPos pos) throws Exception {
        String[] urls = this.urls(pos.x, pos.z);

        if (urls == null || urls.length == 0) { //no urls for tile
            return CompletableFuture.completedFuture(null);
        }

        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        this.addProperties(pos.x, pos.z, builder);
        Map<String, String> properties = builder.build();

        return Http.getFirst(
                Arrays.stream(urls).map(url -> Http.formatUrl(properties, url)).toArray(String[]::new),
                data -> this.decode(pos.x, pos.z, data));
    }
}
