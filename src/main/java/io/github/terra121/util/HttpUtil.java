package io.github.terra121.util;

import com.google.common.base.Preconditions;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles sending and caching of HTTP requests.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class HttpUtil {
    private static final DirectDB DB;

    static {
        File cacheRoot = getCacheRoot();
        try {
            TerraMod.LOGGER.info("Opening cache DB at {}", cacheRoot);
            Preconditions.checkState(cacheRoot.exists() || cacheRoot.mkdirs(), "unable to create directory: %s", cacheRoot);
            DB = LevelDB.PROVIDER.open(getCacheRoot(), new Options()
                    .compressionType(CompressionType.SNAPPY));
            TerraMod.LOGGER.info("Cached DB opened successfully.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to open cache DB at " + cacheRoot, e);
        }
    }

    /**
     * Attempts to GET an array of URLs in order, returning the parsed response body of the first successful one.
     *
     * @param urls the URLs
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
     * @param url the URL
     * @param parseFunction a function to use to parse the response body
     * @return the parsed response body
     * @throws IOException if an I/O exception occurred while attempting to get the data
     */
    public static <T> T getSingle(@NonNull String url, @NonNull EFunction<ByteBuf, T> parseFunction) throws Exception {
        URL parsedUrl = new URL(url);
        boolean canCache = TerraConfig.data.cache && !"file".equals(parsedUrl.getProtocol()); //we don't want to cache local files, because they're already on disk

        Exception cause = null;
        ByteBuf key = Unpooled.wrappedBuffer(url.getBytes(StandardCharsets.UTF_8));

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            //intern the string to make its identity global
            //synchronizing on the interned string makes for a simple global mutex, preventing the same URL from
            // being requested by multiple threads at once
            synchronized (url = url.intern()) {
                if (canCache && DB.getInto(key, buf)) { //url was found in cache
                    TerraMod.LOGGER.info("Cache hit: {}", url);
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
                            DB.put(key, buf.readerIndex(0));
                        }
                        return parsed;
                    } catch (FileNotFoundException e) { //server returned 404 Not Found
                        if (canCache) { //write missing entry to cache
                            DB.put(key, buf.clear().writeBoolean(false));
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

    private static File getCacheRoot() {
        File mcRoot = FMLCommonHandler.instance().getSide().isClient()
                ? Minecraft.getMinecraft().gameDir
                : FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
        return new File(mcRoot, "terraplusplus/cache");
    }
}
