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
    public static Int2ObjectMap<BVH<WrappedUrl>> parseMultiresConfig(@NonNull InputStream src) throws IOException {
        class TempWrappedUrl {
            private String url;
            private double minX = Double.NaN;
            private double maxX = Double.NaN;
            private double minZ = Double.NaN;
            private double maxZ = Double.NaN;
            private int zoom = -1;
            private int minZoom = -1;
            private int maxZoom = -1;
            private double priority = 0.0d;

            public Stream<WrappedUrl> toWrapped() {
                checkState(this.url != null, "url must be set!");
                checkState(!Double.isNaN(this.minX), "minX must be set!");
                checkState(!Double.isNaN(this.maxX), "maxX must be set!");
                checkState(!Double.isNaN(this.minZ), "minZ must be set!");
                checkState(!Double.isNaN(this.maxZ), "maxZ must be set!");
                double minX = min(this.minX, this.maxX);
                double maxX = max(this.minX, this.maxX) - 1; //subtract 1 because bounds are exclusive
                double minZ = min(this.minZ, this.maxZ);
                double maxZ = max(this.minZ, this.maxZ) - 1;

                int minZoom;
                int maxZoom;
                if (this.zoom >= 0) {
                    checkState(this.minZoom == -1 && this.maxZoom == -1, "minZoom/maxZoom may not be used together with zoom!");
                    minZoom = maxZoom = this.zoom;
                } else {
                    checkState(this.minZoom >= 0, "minZoom must be set!");
                    checkState(this.maxZoom >= 0, "minZoom must be set!");
                    minZoom = min(this.minZoom, this.maxZoom);
                    maxZoom = max(this.minZoom, this.maxZoom);
                }
                return IntStream.rangeClosed(minZoom, maxZoom)
                        .mapToObj(zoom -> {
                            double scale = 1.0d / (1 << zoom);
                            return new WrappedUrl(this.url, minX * scale, maxX * scale, minZ * scale, maxZ * scale, zoom, this.priority);
                        });
            }
        }

        NavigableMap<Integer, List<WrappedUrl>> urlsIn;
        try (Reader reader = new UTF8FileReader(src)) {
            urlsIn = Arrays.stream(new GsonBuilder().setLenient().create().fromJson(reader, TempWrappedUrl[].class))
                    .flatMap(TempWrappedUrl::toWrapped)
                    .collect(Collectors.groupingBy(WrappedUrl::zoom, TreeMap::new, Collectors.toList()));
        }

        checkState(!urlsIn.isEmpty(), "no datasets found in \"%s\"", src);

        int minZoom = urlsIn.firstKey();
        int maxZoom = urlsIn.lastKey();
        Int2ObjectMap<BVH<WrappedUrl>> out = new Int2ObjectOpenHashMap<>();
        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            List<WrappedUrl> urls = urlsIn.get(zoom);
            if (urls != null) {
                urls.sort(Comparator.reverseOrder());
                out.put(zoom, new BVH<>(urls));
            }
        }
        return out;
    }

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

    protected abstract BVH<WrappedUrl> urls(int zoom);

    protected String[] urls(int tileX, int tileZ, int zoom) {
        double scale = this.tileSize / (1 << zoom);
        BVH<WrappedUrl> urls = this.urls(zoom);
        if (urls == null) {
            return null;
        }
        return urls.getAllIntersecting(Bounds2d.of(tileX * scale, (tileX + 1) * scale, tileZ * scale, (tileZ + 1) * scale))
                .stream().sorted().map(WrappedUrl::url).toArray(String[]::new);
    }

    protected void addProperties(int tileX, int tileZ, int zoom, @NonNull ImmutableMap.Builder<String, String> builder) {
        builder.put("x", String.valueOf(tileX))
                .put("z", String.valueOf(tileZ))
                .put("zoom", String.valueOf(zoom));
    }

    protected abstract T decode(int tileX, int tileZ, int level, @NonNull ByteBuf data) throws Exception;

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
        String[] urls = this.urls(pos.getX(), pos.getZ(), pos.getY());

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
