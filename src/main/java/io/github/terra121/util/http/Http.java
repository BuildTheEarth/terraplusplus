package io.github.terra121.util.http;

import com.google.common.base.Preconditions;
import io.github.terra121.TerraConfig;
import io.github.terra121.TerraMod;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AttributeKey;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.ldbjni.LevelDB;
import net.daporkchop.ldbjni.direct.DirectDB;
import net.daporkchop.lib.common.function.throwing.EFunction;
import net.daporkchop.lib.common.misc.threadfactory.PThreadFactories;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.Options;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles sending and caching of HTTP requests.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class Http {
    protected final AttributeKey<Boolean> ATTR_CHANNEL_FULL = AttributeKey.newInstance("terra++_http_channel_full");

    private final ThreadFactory NETWORK_THREAD_FACTORY = PThreadFactories.builder().daemon().minPriority()
            .name("terra++ HTTP network thread").build();
    private final ThreadFactory WORKER_THREAD_FACTORY = PThreadFactories.builder().daemon().minPriority().collapsingId()
            .name("terra++ HTTP worker thread #%d").build();

    protected final EventLoop NETWORK_EVENT_LOOP = (Epoll.isAvailable()
            ? new EpollEventLoopGroup(1, NETWORK_THREAD_FACTORY) //use epoll on linux systems wherever possible
            : new NioEventLoopGroup(1, NETWORK_THREAD_FACTORY)).next(); //this is fine because there's always exactly one network worker

    protected final Bootstrap DEFAULT_BOOTSTRAP = new Bootstrap()
            .group(NETWORK_EVENT_LOOP)
            .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.SO_TIMEOUT, 5)
            .attr(ATTR_CHANNEL_FULL, false);

    protected final SslContext SSL_CONTEXT;

    protected final Map<Host, HostManager> MANAGERS = new ConcurrentHashMap<>();

    private final DirectDB DB;

    static {
        try {
            SSL_CONTEXT = SslContextBuilder.forClient()
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (SSLException e) {
            throw new RuntimeException("unable to create ssl context", e);
        }
    }

    static {
        File cacheRoot = getCacheRoot();
        try {
            if (TerraMod.LOGGER != null) {
                TerraMod.LOGGER.info("Opening cache DB at {}", cacheRoot);
            }
            Preconditions.checkState(cacheRoot.exists() || cacheRoot.mkdirs(), "unable to create directory: %s", cacheRoot);
            DB = LevelDB.PROVIDER.open(cacheRoot, new Options().compressionType(CompressionType.SNAPPY));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open cache DB at " + cacheRoot, e);
        }
    }

    public void configChanged() {
    }

    protected HostManager managerFor(@NonNull URL url) {
        return MANAGERS.computeIfAbsent(new Host(url), host -> new HostManager(host, 1));
    }

    public static CompletableFuture<ByteBuf> get(@NonNull String url) {
        try {
            URL parsed = new URL(url);
            CompletableFuture<ByteBuf> future = new CompletableFuture<>();
            managerFor(parsed).submit(parsed.getFile(), future);
            return future;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(url, e);
        }
    }

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
        boolean canCache = TerraConfig.data.cache && !"file".equals(parsedUrl.getProtocol()); //we don't want to cache local files, because they're already on disk

        Exception cause = null;
        ByteBuf key = Unpooled.wrappedBuffer(url.getBytes(StandardCharsets.UTF_8));

        long now = System.currentTimeMillis();
        long ttl = TimeUnit.MINUTES.toMillis(TerraConfig.data.cacheTTL);

        ByteBuf cachedValue = canCache ? DB.getZeroCopy(key) : null;
        try {
            if (cachedValue != null && cachedValue.readLong() + ttl < now) { //cache hit, and the cached value hasn't expired yet
                if (!TerraConfig.reducedConsoleMessages) {
                    TerraMod.LOGGER.info("Cache hit: {}", url);
                }
                if (cachedValue.readBoolean()) { //cached value exists
                    return parseFunction.applyThrowing(cachedValue);
                } else { //we cached a 404
                    return null;
                }
            }

            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
            try {
                //intern the string to make its identity global
                //synchronizing on the interned string makes for a simple global mutex, preventing the same URL from
                // being requested by multiple threads at once
                synchronized (url = url.intern()) {
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
                                buf.clear().writeLong(now).writeBoolean(true);
                                do {
                                    buf.ensureWritable(1024);
                                } while (buf.writeBytes(in, 1024) > 0);
                            }

                            T parsed = parseFunction.applyThrowing(buf.skipBytes(9));

                            if (canCache) { //write data to cache
                                DB.put(key, buf.readerIndex(0));
                            }
                            return parsed;
                        } catch (FileNotFoundException e) { //server returned 404 Not Found
                            if (canCache) { //write missing entry to cache
                                DB.put(key, buf.clear().writeLong(now).writeBoolean(false));
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

            if (cachedValue != null) { //we still have a value from the cache. it's expired, but we'll return it anyway because we were unable to fetch anything
                if (!TerraConfig.reducedConsoleMessages) {
                    TerraMod.LOGGER.info("Falling back to expired cached value: {}", url);
                }
                if (cachedValue.readBoolean()) { //cached value exists
                    return parseFunction.applyThrowing(cachedValue);
                } else { //we cached a 404
                    return null;
                }
            } else if (cause != null) {
                throw cause;
            } else {
                throw new IllegalStateException("0 retries?!?");
            }
        } finally {
            if (cachedValue != null) {
                cachedValue.release();
            }
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
        File mcRoot;
        try {
            mcRoot = FMLCommonHandler.instance().getSide().isClient()
                    ? Minecraft.getMinecraft().gameDir
                    : FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
        } catch (NullPointerException e) { //this probably means we're running in a test environment, and FML isn't initialized
            mcRoot = new File("/tmp");
        }
        return new File(mcRoot, "terraplusplus/cache");
    }
}
