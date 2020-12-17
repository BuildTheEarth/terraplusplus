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
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.function.throwing.EFunction;
import net.daporkchop.lib.common.misc.threadfactory.PThreadFactories;

import javax.net.ssl.SSLException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.daporkchop.lib.common.util.PValidation.*;

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
     */
    public static <T> CompletableFuture<T> getFirst(@NonNull String[] urls, @NonNull EFunction<ByteBuf, T> parseFunction) {
        checkArg(urls.length > 0, "must provide at least one url");

        if (urls.length == 1) {
            return getSingle(urls[0], parseFunction);
        }

        class State implements BiConsumer<T, Throwable> {
            final CompletableFuture<T> future = new CompletableFuture<>();
            RuntimeException e = null;

            /**
             * The current iteration index.
             */
            int i = -1;

            /**
             * Whether or not any of the URLs completed successfully, but returned {@code 404 Not Found}.
             */
            boolean foundMissing = false;

            @Override
            public void accept(T value, Throwable cause) {
                if (cause != null) {
                    if (this.e == null) {
                        this.e = new RuntimeException("All URLs completed exceptionally!");
                    }
                    this.e.addSuppressed(cause);
                } else if (value == null) { //remember that one of the URLs 404'd
                    this.foundMissing = true;
                } else { //complete the future successfully with the retrieved value
                    this.future.complete(value);
                    return;
                }

                this.advance();
            }

            protected void advance() {
                if (++this.i < urls.length) {
                    getSingle(urls[this.i], parseFunction).whenComplete(this);
                } else if (this.foundMissing) { //the best result from any of the URLs was a 404
                    this.future.complete(null);
                } else {
                    this.future.completeExceptionally(this.e);
                }
            }
        }

        State state = new State();
        state.advance();
        return state.future;
    }

    /**
     * Attempts to GET a single URL.
     *
     * @param url           the URL
     * @param parseFunction a function to use to parse the response body
     * @return the parsed response body
     */
    public static <T> CompletableFuture<T> getSingle(@NonNull String url, @NonNull EFunction<ByteBuf, T> parseFunction) {
        return get(url)
                .thenCompose(buf -> buf == null
                        ? CompletableFuture.completedFuture(null)
                        : CompletableFuture.supplyAsync(() -> {
                    try {
                        return parseFunction.apply(buf);
                    } finally {
                        buf.release();
                    }
                }));
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
