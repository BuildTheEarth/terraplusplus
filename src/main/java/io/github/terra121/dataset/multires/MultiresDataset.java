package io.github.terra121.dataset.multires;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.bvh.BVH;
import io.github.terra121.util.bvh.Bounds2d;
import io.github.terra121.util.bvh.Bounds2i;
import io.github.terra121.util.http.Http;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.NonNull;
import net.daporkchop.lib.binary.oio.reader.UTF8FileReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

public abstract class MultiresDataset<T> extends CacheLoader<CubePos, CompletableFuture<T>> {
    protected final LoadingCache<CubePos, CompletableFuture<T>> cache = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .build(this);

    protected final double tileSize;

    protected final GeographicProjection projection;

    public MultiresDataset(GeographicProjection proj, double tileSize) {
        this.projection = proj;
        this.tileSize = tileSize;
    }

    protected abstract MultiresConfig config();

    protected void addProperties(int tileX, int tileZ, int zoom, @NonNull ImmutableMap.Builder<String, String> builder) {
        double tileSize = this.tileSize / (1 << zoom);
        builder.put("x", String.valueOf(tileX))
                .put("z", String.valueOf(tileZ))
                .put("zoom", String.valueOf(zoom))
                .put("lon.min", String.format("%.12f", tileX * tileSize))
                .put("lon.max", String.format("%.12f", (tileX + 1) * tileSize))
                .put("lat.min", String.format("%.12f", tileZ * tileSize))
                .put("lat.max", String.format("%.12f", (tileZ + 1) * tileSize));
    }

    protected abstract T decode(int tileX, int tileZ, int zoom, @NonNull ByteBuf data) throws Exception;

    public CompletableFuture<T> getTileAsync(int tileX, int tileZ, int zoom) {
        return this.getTileAsync(new CubePos(tileX, tileZ, zoom));
    }

    public CompletableFuture<T> getTileAsync(@NonNull CubePos pos) {
        return this.cache.getUnchecked(pos);
    }

    /**
     * internal API, don't call this method directly!
     */
    @Deprecated
    @Override
    public CompletableFuture<T> load(CubePos pos) throws Exception {
        System.out.println("loading tile at " + pos);
        String[] urls = this.config().getUrls(pos.getX(), pos.getZ(), pos.getY());

        if (urls == null || urls.length == 0) { //no urls for tile
            return CompletableFuture.completedFuture(null);
        }

        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        this.addProperties(pos.getX(), pos.getZ(), pos.getY(), builder);
        Map<String, String> properties = builder.build();

        return Http.getFirst(
                Arrays.stream(urls).map(url -> Http.formatUrl(properties, url)).toArray(String[]::new),
                data -> this.decode(pos.getX(), pos.getZ(), pos.getY(), data));
    }
}
