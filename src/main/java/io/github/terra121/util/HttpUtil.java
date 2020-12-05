package io.github.terra121.util;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import io.github.terra121.TerraConfig;
import io.github.terra121.TerraMod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.ldbjni.LevelDB;
import net.daporkchop.ldbjni.direct.DirectDB;
import net.daporkchop.lib.common.function.throwing.EFunction;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles sending and caching of HTTP requests.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class HttpUtil {
    private static final LoadingCache<String, DirectDB> DBS = CacheBuilder.newBuilder()
            .expireAfterAccess(5L, TimeUnit.MINUTES)
            .removalListener((RemovalListener<String, DirectDB>) notification -> {
                File cacheRoot = new File(getCacheRoot(), notification.getKey());
                try {
                    TerraMod.LOGGER.info("Closing cache DB at {}", cacheRoot);
                    notification.getValue().close();
                } catch (IOException e) {
                    throw new RuntimeException("Unable to close cache DB at " + cacheRoot, e);
                }
            })
            .build(new CacheLoader<String, DirectDB>() {
                @Override
                public DirectDB load(String key) throws Exception {
                    File cacheRoot = new File(getCacheRoot(), key);
                    try {
                        TerraMod.LOGGER.info("Opening cache DB at {}", cacheRoot);
                        Preconditions.checkState(cacheRoot.exists() || cacheRoot.mkdirs(), "unable to create directory: %s", cacheRoot);
                        return LevelDB.PROVIDER.open(cacheRoot, new Options().compressionType(CompressionType.SNAPPY));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to open cache DB at " + cacheRoot, e);
                    }
                }
            });

    /**
     * Attempts to GET an array of URLs in order, returning the parsed response body of the first successful one.
     *
     * @param urls          the URLs
     * @param parseFunction a function to use to parse the response body
     * @return the parsed response body
     * @throws IOException if no URLs were given, or all returned an error code
     */
    public static <T> T getFirst(@NonNull String[] urls, @NonNull EFunction<ByteBuf, T> parseFunction) throws Exception {
        Exception cause = null;
        boolean found404 = false;

        for (String url : urls) {
            try {
                return getSingle(url, parseFunction);
            } catch (FileNotFoundException e) {
                found404 = true;
            } catch (Exception e) {
                if (cause == null) {
                    cause = new Exception();
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

    /**
     * Attempts to GET a single URL.
     *
     * @param url           the URL
     * @param parseFunction a function to use to parse the response body
     * @return the parsed response body
     * @throws IOException if an I/O exception occurred while attempting to get the data
     */
    public static <T> T getSingle(@NonNull String url, @NonNull EFunction<ByteBuf, T> parseFunction) throws Exception {
        URL parsedUrl = new URL(url);
        String dbKey = getCacheDBName(parsedUrl);
        boolean canCache = TerraConfig.data.cache && !"file".equals(parsedUrl.getProtocol()); //we don't want to cache local files, because they're already on disk

        Exception cause = null;
        ByteBuf key = Unpooled.wrappedBuffer(url.getBytes(StandardCharsets.UTF_8));

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            //intern the string to make its identity global
            //synchronizing on the interned string makes for a simple global mutex, preventing the same URL from
            // being requested by multiple threads at once
            synchronized (url = url.intern()) {
                if (canCache && DBS.getUnchecked(dbKey).getInto(key, buf)) { //url was found in cache
                    if (!TerraConfig.reducedConsoleMessages) {
                        TerraMod.LOGGER.info("Cache hit: {}", url);
                    }
                    if (buf.readBoolean()) { //cached value exists
                        return parseFunction.applyThrowing(buf);
                    } else { //we cached a 404
                        return null;
                    }
                }

                for (int i = 0; i < TerraConfig.data.retryCount; i++) {
                    if (!TerraConfig.reducedConsoleMessages) {
                        TerraMod.LOGGER.info("GET #{}: {}", i, url);
                    }

                    try {
                        URLConnection conn = parsedUrl.openConnection();
                        conn.addRequestProperty("User-Agent", TerraMod.USERAGENT);
                        conn.setConnectTimeout(TerraConfig.data.timeout);
                        conn.setReadTimeout(TerraConfig.data.timeout);
                        if (conn instanceof HttpURLConnection) {
                            ((HttpURLConnection) conn).setInstanceFollowRedirects(true);
                        }

                        try (InputStream in = conn.getInputStream()) {
                            buf.clear().writeBoolean(true);
                            do {
                                buf.ensureWritable(1024);
                            } while (buf.writeBytes(in, 1024) > 0);
                        }

                        T parsed = parseFunction.applyThrowing(buf.skipBytes(1));

                        if (canCache) { //write data to cache
                            DBS.getUnchecked(dbKey).put(key, buf.readerIndex(0));
                        }
                        return parsed;
                    } catch (FileNotFoundException e) { //server returned 404 Not Found
                        if (canCache) { //write missing entry to cache
                            DBS.getUnchecked(dbKey).put(key, buf.clear().writeBoolean(false));
                        }
                        throw e;
                    } catch (Exception e) {
                        if (cause == null) {
                            cause = new Exception("Unable to GET " + url);
                        }
                        cause.addSuppressed(e);
                    }
                }
            }
        } finally {
            buf.release();
        }

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

    public static String getCacheDBName(@NonNull URL url) {
        return url.getProtocol() + '/' + url.getHost().replaceAll("[\\\\?%*:|\"<>]", "_");
    }

    private static File getCacheRoot() {
        File mcRoot = FMLCommonHandler.instance().getSide().isClient()
                ? Minecraft.getMinecraft().gameDir
                : FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
        return new File(mcRoot, "terraplusplus/cache");
    }
}
