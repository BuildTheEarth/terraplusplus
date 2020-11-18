package io.github.terra121.dataset;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import io.github.terra121.TerraConfig;
import io.github.terra121.TerraMod;
import io.github.terra121.projection.GeographicProjection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TiledDataset<T> {
    public static ByteBuf get(int tileX, int tileZ, @NonNull Map<String, String> properties, @NonNull String[] urls) throws IOException {
        IOException cause = null;
        for (String url : urls) {
            url = formatUrl(properties, url);
            try {
                return get(url);
            } catch (IOException e) {
                if (cause == null) {
                    cause = new IOException("Unable to fetch tile at " + tileX + ',' + tileZ + " with properties: " + properties);
                }
                cause.addSuppressed(e);
            }
        }

        if (cause != null) {
            throw cause;
        } else {
            throw new IllegalStateException("0 urls?!?");
        }
    }

    public static ByteBuf get(@NonNull String url) throws IOException {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

        IOException cause = null;
        for (int i = 0; i < TerraConfig.data.retryCount; i++) {
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
            } catch (IOException e){
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

    //TODO: better datatypes
    protected LinkedHashMap<Coord, int[]> cache;
    protected int numcache;
    protected final int width;
    protected final int height;
    protected GeographicProjection projection;
    //TODO: scales are obsolete with new ScaleProjection type
    protected double scaleX;
    protected double scaleY;
    protected double[] bounds;

    public TiledDataset(int width, int height, int numcache, GeographicProjection proj, double projScaleX, double projScaleY, boolean smooth) {
        this.cache = new LinkedHashMap<>();
        this.numcache = numcache;
        this.width = width;
        this.height = height;
        this.projection = proj;
        this.scaleX = projScaleX;
        this.scaleY = projScaleY;

        this.bounds = proj.bounds();
        this.bounds[0] *= this.scaleX;
        this.bounds[1] *= this.scaleY;
        this.bounds[2] *= this.scaleX;
        this.bounds[3] *= this.scaleY;
    }

    public TiledDataset(int width, int height, int numcache, GeographicProjection proj, double projScaleX, double projScaleY) {
        this(width, height, numcache, proj, projScaleX, projScaleY, false);
    }

    protected abstract String[] urls();

    protected abstract void addProperties(int tileX, int tileZ, @NonNull ImmutableMap.Builder<String, String> builder);

    protected abstract T decode(int tileX, int tileZ, @NonNull ByteBuf data) throws Exception;

    public T getTile(int tileX, int tileZ) {
        //TODO: in-memory and persistent cache

        try {
            ByteBuf data = this.fetchTile(tileX, tileZ);
            try {
                return this.decode(tileX, tileZ, data);
            } finally { //avoid memory leak in the case of failure by always releasing the data
                data.release();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ByteBuf fetchTile(int tileX, int tileZ) throws IOException {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                .put("tile.x", String.valueOf(tileX))
                .put("tile.z", String.valueOf(tileZ));
        this.addProperties(tileX, tileZ, builder);

        return get(tileX, tileZ, builder.build(), this.urls());
    }

    //integer coordinate class for tile coords and pixel coords
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    protected class Coord {
        public int x;
        public int y;

        protected Coord tile() {
            return new Coord(this.x / TiledDataset.this.width, this.y / TiledDataset.this.height);
        }
    }
}
