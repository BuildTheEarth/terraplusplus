package io.github.terra121.util.http;

import com.google.common.base.Preconditions;
import io.github.terra121.TerraConfig;
import io.github.terra121.TerraMod;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
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
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.function.throwing.EFunction;
import net.daporkchop.lib.common.misc.threadfactory.PThreadFactories;

import javax.net.ssl.SSLException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles sending and caching of HTTP requests.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class Http {
    private final ThreadFactory NETWORK_THREAD_FACTORY = PThreadFactories.builder().daemon().minPriority().name("terra++ HTTP network thread").build();

    protected final EventLoop NETWORK_EVENT_LOOP = (Epoll.isAvailable()
            ? new EpollEventLoopGroup(1, NETWORK_THREAD_FACTORY) //use epoll on linux systems wherever possible
            : new NioEventLoopGroup(1, NETWORK_THREAD_FACTORY)).next(); //this is fine because there's always exactly one network worker

    protected final Bootstrap DEFAULT_BOOTSTRAP = new Bootstrap()
            .group(NETWORK_EVENT_LOOP)
            .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true);

    protected final SslContext SSL_CONTEXT;

    protected final Map<Host, HostManager> MANAGERS = new ConcurrentHashMap<>();

    protected final int MAX_CONTENT_LENGTH = Integer.MAX_VALUE; //impossibly large, no requests will actually be this big but whatever

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

    private HostManager managerFor(@NonNull URL url) {
        return MANAGERS.computeIfAbsent(new Host(url), HostManager::new);
    }

    /**
     * Asynchronously gets the contents of the given resource.
     *
     * @param url the url of the resource to get
     * @return a {@link CompletableFuture} which will be completed with the resource data, or {@code null} if the resource isn't found
     */
    public CompletableFuture<ByteBuf> get(@NonNull String url) {
        try {
            URL parsed = new URL(url);

            if ("file".equalsIgnoreCase(parsed.getProtocol())) { //it's a file, read from disk (also async)
                return Disk.read(Paths.get(parsed.getFile()), false);
            }

            Path cacheFile = Disk.cacheFileFor(parsed.toString());
            return Disk.read(cacheFile, true)
                    .thenCompose(cachedData -> {
                        if (TerraMod.LOGGER != null && !TerraConfig.reducedConsoleMessages) {
                            TerraMod.LOGGER.info("Cache {}: {}", cachedData != null ? "hit" : "miss", url);
                        }
                        if (cachedData != null) { //we found something in the cache
                            //first byte is a boolean indicating whether or not the cached value is a 404
                            return CompletableFuture.completedFuture(cachedData.readBoolean() ? cachedData : null);
                        } else { //cache miss
                            CompletableFuture<ByteBuf> future = new CompletableFuture<>();
                            managerFor(parsed).submit(parsed.getFile(), future);

                            future.thenAccept(requestedData -> { //store the response body in the cache
                                //prefix data with 404 flag
                                ByteBuf toCacheData = ByteBufAllocator.DEFAULT.ioBuffer().writeBoolean(requestedData != null);
                                if (requestedData != null) { //append the actual data
                                    toCacheData.writeBytes(requestedData, requestedData.readerIndex(), requestedData.readableBytes());
                                }
                                Disk.write(cacheFile, toCacheData);
                            });
                            return future;
                        }
                    });
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(url, e);
        }
    }

    /**
     * Sets the maximum number of concurrent requests to the given remote host.
     *
     * @param host                  the host. May be any valid URL, however only the protocol and authority components will be considered
     * @param maxConcurrentRequests the new maximum number of concurrent requests to the host
     */
    public void setMaximumConcurrentRequestsTo(@NonNull String host, int maxConcurrentRequests) {
        try {
            managerFor(new URL(host)).setMaxConcurrentRequests(maxConcurrentRequests);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(host, e);
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
    public static <T> CompletableFuture<T> getFirst(@NonNull String[] urls, @NonNull EFunction<ByteBuf, T> parseFunction) throws Exception {
        //TODO: make this actually async

        Exception cause = null;
        boolean found404 = false;

        for (String url : urls) {
            try {
                return CompletableFuture.completedFuture(getSingle(url, parseFunction));
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
            return CompletableFuture.completedFuture(null);
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
    private static <T> T getSingle(@NonNull String url, @NonNull EFunction<ByteBuf, T> parseFunction) throws Exception {
        ByteBuf buf = get(url).join();
        try {
            return buf != null ? parseFunction.applyThrowing(buf) : null;
        } finally {
            ReferenceCountUtil.release(buf);
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

    public void configChanged() {
        Matcher matcher = Pattern.compile("^(\\d+): (.+)$").matcher("");
        for (String entry : TerraConfig.data.maxConcurrentRequests) {
            if (matcher.reset(entry).matches()) {
                try {
                    setMaximumConcurrentRequestsTo(matcher.group(2), Integer.parseInt(matcher.group(1)));
                } catch (Exception e) {
                    TerraMod.LOGGER.error("Invalid entry: \"" + entry + '"', e);
                }
            } else {
                TerraMod.LOGGER.warn("Invalid entry: \"{}\"", entry);
            }
        }
    }

    protected <T> void copyResultTo(@NonNull CompletableFuture<T> src, @NonNull CompletableFuture<T> dst) {
        src.whenComplete((v, t) -> {
            if (t != null) {
                dst.completeExceptionally(t);
            } else {
                dst.complete(v);
            }
        });
    }
}
