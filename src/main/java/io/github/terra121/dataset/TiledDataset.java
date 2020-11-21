package io.github.terra121.dataset;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.github.terra121.TerraConfig;
import io.github.terra121.TerraMod;
import io.github.terra121.projection.GeographicProjection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TiledDataset<T> extends CacheLoader<ChunkPos, T> {
    public static ByteBuf get(int tileX, int tileZ, @NonNull Map<String, String> properties, @NonNull String[] urls) throws IOException {
        IOException cause = null;
        boolean found404 = false;

        for (String url : urls) {
            url = formatUrl(properties, url);
            try {
                ByteBuf buf = get(url);
                if (buf != null) {
                    return buf;
                } else {
                    found404 = true;
                }
            } catch (IOException e) {
                if (cause == null) {
                    cause = new IOException("Unable to fetch tile at " + tileX + ',' + tileZ + " with properties: " + properties);
                }
                cause.addSuppressed(e);
            }
        }

        if (found404) {
            //one of the urls was successful, but returned 404 Not Found
            return null;
        } else if (cause != null) {
            throw cause;
        } else {
            throw new IllegalStateException("0 urls?!?");
        }
    }

    public static ByteBuf get(@NonNull String url) throws IOException {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

        IOException cause = null;
        for (int i = 0; i < TerraConfig.data.retryCount; i++) {
            if (!TerraConfig.reducedConsoleMessages) {
                TerraMod.LOGGER.info(url);
            }

            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.addRequestProperty("User-Agent", TerraMod.USERAGENT);
                try (InputStream in = conn.getInputStream()) {
                    buf.clear();
                    do {
                        buf.ensureWritable(1024);
                    } while (buf.writeBytes(in, 1024) > 0);
                }
                return buf;
            } catch (FileNotFoundException e) { //server returned 404 Not Found
                buf.release();
                return null;
            } catch (IOException e) {
                if (cause == null) {
                    cause = new IOException("Unable to GET " + url);
                }
                cause.addSuppressed(e);
            }
        }

        buf.release();

        if (cause != null) {
            throw cause;
        } else {
            throw new IllegalStateException("0 retries?!?");
        }
    }

    public static String formatUrl(@NonNull Map<String, String> properties, @NonNull String url) {
        StringBuffer out = new StringBuffer();
        Matcher matcher = Pattern.compile("\\$\\{([a-z0-9.]+)}").matcher(url);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = properties.get(key);
            Preconditions.checkArgument(value != null, "unknown property: \"%s\"", key);
            matcher.appendReplacement(out, value);
        }
        matcher.appendTail(out);
        return out.toString();
    }

    protected final LoadingCache<ChunkPos, T> cache = CacheBuilder.newBuilder()
            .softValues()
            .build(this);

    protected final double tileSize;
    protected final double scale;

    protected final GeographicProjection projection;
    //TODO: scales are obsolete with new ScaleProjection type
    protected final double[] bounds;

    public TiledDataset(GeographicProjection proj, double tileSize, double scale) {
        this.projection = proj;
        this.tileSize = tileSize;
        this.scale = scale;

        this.bounds = proj.bounds();
        this.bounds[0] *= this.scale;
        this.bounds[1] *= this.scale;
        this.bounds[2] *= this.scale;
        this.bounds[3] *= this.scale;
    }

    protected abstract String[] urls();

    protected void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder) {
        builder.put("x", String.valueOf(tileX))
                .put("z", String.valueOf(tileZ))
                .put("lon.min", String.format("%.12f", tileX * this.tileSize))
                .put("lon.max", String.format("%.12f", (tileX + 1) * this.tileSize))
                .put("lat.min", String.format("%.12f", tileZ * this.tileSize))
                .put("lat.max", String.format("%.12f", (tileZ + 1) * this.tileSize));
    }

    protected abstract T decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception;

    public T getTile(int tileX, int tileZ) {
        return this.cache.getUnchecked(new ChunkPos(tileX, tileZ));
    }

    /**
     * internal API, don't call this method directly!
     */
    @Deprecated
    @Override
    public T load(ChunkPos pos) throws Exception {
        ByteBuf data = this.fetchTile(pos.x, pos.z);
        try {
            return this.decode(pos.x, pos.z, data);
        } finally { //avoid memory leak in the case of failure by always releasing the data
            data.release();
        }
    }

    public ByteBuf fetchTile(int tileX, int tileZ) throws IOException {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        this.addProperties(tileX, tileZ, builder);

        return get(tileX, tileZ, builder.build(), this.urls());
    }
}
