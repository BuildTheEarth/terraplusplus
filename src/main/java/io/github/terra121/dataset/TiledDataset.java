package io.github.terra121.dataset;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.http.Http;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static net.daporkchop.lib.common.math.PMath.*;

public abstract class TiledDataset<T> extends CacheLoader<ChunkPos, CompletableFuture<T>> {
    protected final LoadingCache<ChunkPos, CompletableFuture<T>> cache = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .build(this);

    protected final double tileSize;
    protected final double scale;

    protected final GeographicProjection projection;
    protected final int minSampleX;
    protected final int maxSampleX;

    public TiledDataset(GeographicProjection proj, double tileSize, double scale) {
        this.projection = proj;
        this.tileSize = tileSize;
        this.scale = scale;

        double[] bounds = proj.bounds();
        this.minSampleX = floorI(bounds[0] * this.scale);
        this.maxSampleX = ceilI(bounds[2] * this.scale);
    }

    protected abstract String[] urls(int tileX, int tileZ);

    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        builder.put("x", String.valueOf(tileX))
                .put("z", String.valueOf(tileZ))
                .put("lon.min", String.format("%.12f", tileX * this.tileSize))
                .put("lon.max", String.format("%.12f", (tileX + 1) * this.tileSize))
                .put("lat.min", String.format("%.12f", tileZ * this.tileSize))
                .put("lat.max", String.format("%.12f", (tileZ + 1) * this.tileSize));
    }

    protected abstract T decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception;

    @Deprecated
    public T getTile(int tileX, int tileZ) {
        return this.getTileAsync(tileX, tileZ).join();
    }

    public CompletableFuture<T> getTileAsync(int tileX, int tileZ) {
        return this.cache.getUnchecked(new ChunkPos(tileX, tileZ));
    }

    /**
     * internal API, don't call this method directly!
     */
    @Deprecated
    @Override
    public CompletableFuture<T> load(ChunkPos pos) throws Exception {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        this.addProperties(pos.x, pos.z, builder);
        Map<String, String> properties = builder.build();

        return Http.getFirst(
                Arrays.stream(this.urls(pos.x, pos.z)).map(url -> Http.formatUrl(properties, url)).toArray(String[]::new),
                data -> this.decode(pos.x, pos.z, data));
    }
}
