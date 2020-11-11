package io.github.terra121;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration for the remote servers used by T++ for fetching data.
 *
 * @author DaPorkchop_
 */
@Getter
public class ServerConfig {
    private static ServerConfig INSTANCE;

    public static ServerConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public synchronized static void load() {
    }

    private ServerEntry[] trees;

    @Getter
    @ToString
    public static class ServerEntry {
        private String url;
        private int retryCount;

        public ByteBuf fetch(@NonNull TilePos pos) throws IOException {
            String url = this.urlForPos(pos);

            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
            IOException cause = null;
            for (int i = 0; i < this.retryCount; i++) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.addRequestProperty("User-Agent", TerraMod.USERAGENT);
                    try (InputStream in = conn.getInputStream()) {
                        buf.clear();

                    }
                } catch (IOException e){
                }
            }
        }

        public String urlForPos(@NonNull TilePos pos) {
            Map<String, String> properties = pos.toPropertyMap();

            StringBuffer out = new StringBuffer();
            Matcher matcher = Pattern.compile("\\$\\{([a-z0-9.]+)}").matcher(this.url);
            while (matcher.find()) {
                String key = matcher.group(1);
                String value = properties.get(key);
                Preconditions.checkArgument(value != null, "unknown property: \"%s\"", key);
                matcher.appendReplacement(out, value);
            }
            matcher.appendTail(out);
            return out.toString();
        }

        /**
         * The position of a remote data tile.
         *
         * @author DaPorkchop_
         */
        @Getter
        @Setter
        @ToString
        public static class TilePos {
            private int x;
            private int z;
            private double minLat;
            private double maxLat;
            private double minLon;
            private double maxLon;

            public Map<String, String> toPropertyMap() {
                return ImmutableMap.<String, String>builder()
                        .put("tile.x", String.valueOf(this.x))
                        .put("tile.z", String.valueOf(this.z))
                        .put("tile.lat.min", String.format("%.12f", this.minLat))
                        .put("tile.lat.max", String.format("%.12f", this.maxLat))
                        .put("tile.lon.min", String.format("%.12f", this.minLon))
                        .put("tile.lon.max", String.format("%.12f", this.maxLon))
                        .build();
            }
        }
    }
}
