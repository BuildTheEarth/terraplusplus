package io.github.terra121.util.http;

import com.google.common.base.Preconditions;
import io.github.terra121.TerraConfig;
import io.github.terra121.TerraMod;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
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
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.ref.ThreadRef;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    protected static final Ref<Matcher> URL_FORMATTING_MATCHER_CACHE = ThreadRef.regex(Pattern.compile("\\$\\{([a-z0-9.]+)}"));

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
        CompletableFuture<ByteBuf> future = new CompletableFuture<>();
        get(url, future);
        return future;
    }

    public void get(@NonNull String _url, @NonNull CompletableFuture<ByteBuf> future) {
        class State implements BiConsumer<ByteBuf, Throwable>, HostManager.Callback {
            URL parsed;
            Path cacheFile;

            @Override
            public boolean isCancelled() {
                return future.isDone();
            }

            @Override
            public void accept(ByteBuf cachedData, Throwable throwable) { //stage 1: handle value from cache
                if (TerraMod.LOGGER != null) {
                    if (throwable != null) {
                        TerraMod.LOGGER.error("Unable to read cache for " + this.parsed, throwable);
                    } else if (!TerraConfig.reducedConsoleMessages) {
                        TerraMod.LOGGER.info("Cache {}: {}", cachedData != null ? "hit" : "miss", this.parsed);
                    }
                }

                try {
                    if (cachedData != null) { //we found something in the cache
                        switch (cachedData.readByte()) {
                            case 0: //404 Not Found
                                future.complete(null);
                                return;
                            case 1: //2xx
                                future.complete(cachedData.retain());
                                return;
                            case 2: //redirect
                                this.step(cachedData.readCharSequence(cachedData.readableBytes(), StandardCharsets.UTF_8).toString());
                                return;
                        }
                    }
                } catch (Exception e) {
                    if (TerraMod.LOGGER != null) {
                        TerraMod.LOGGER.error("Unable to read cache for " + this.parsed, e);
                    }
                } finally {
                    ReferenceCountUtil.release(cachedData);
                }

                //cache miss, send the actual request
                managerFor(this.parsed).submit(this.parsed.getFile(), this);
            }

            @Override
            public void handle(FullHttpResponse response, Throwable throwable) { //stage 2: handle HTTP response
                if (throwable != null) { //if an exception occurred, notify future and exit
                    future.completeExceptionally(throwable);
                } else {
                    Path cacheFile = this.cacheFile; //get here because the field might be modified if the response is a redirect
                    ByteBuf toCacheData;
                    switch (response.status().codeClass()) {
                        case SUCCESS:
                            //notify future first
                            //copy because it's a composite buffer, which would be slow when accessing individual bytes (this is not an issue
                            // when writing to disk, because FileChannel can efficiently write data from multiple buffers at once)
                            future.complete(response.content().copy());

                            toCacheData = Unpooled.compositeBuffer(2)
                                    .addComponent(true, Unpooled.wrappedBuffer(new byte[]{ 1 }))
                                    .addComponent(true, response.content().retain());
                            break;
                        case INFORMATIONAL: //no-op: nothing needs to be done
                            return;
                        case REDIRECTION: { //handle redirect
                            String dst = response.headers().get(HttpHeaderNames.LOCATION);
                            toCacheData = Unpooled.wrappedBuffer(new byte[]{ 2 }, dst.getBytes(StandardCharsets.UTF_8));
                            this.step(dst);
                            break;
                        }
                        case CLIENT_ERROR:
                            if (response.status().code() == 404) { //404 Not Found
                                toCacheData = Unpooled.wrappedBuffer(new byte[]{ 0 });
                                future.complete(null);
                                break;
                            }
                        default: //failure
                            future.completeExceptionally(new IOException("response from server: " + response.status()));
                            return;
                    }

                    //write to disk
                    Disk.write(cacheFile, toCacheData);
                }
            }

            void step(@NonNull String url) {
                try {
                    this.parsed = new URL(url);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(url, e);
                }

                if ("file".equalsIgnoreCase(this.parsed.getProtocol())) { //it's a file, read from disk (also async)
                    copyResultTo(Disk.read(Paths.get(this.parsed.getFile()), false), future);
                    return;
                }

                this.cacheFile = Disk.cacheFileFor(this.parsed.toString());
                Disk.read(this.cacheFile, true).whenComplete(this);
            }
        }

        new State().step(_url);
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
            List<Throwable> suppressed;

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
                    if (this.suppressed == null) {
                        this.suppressed = new ArrayList<>();
                    }
                    this.suppressed.add(new RuntimeException(urls[this.i], cause));
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
                    if (this.suppressed != null) {
                        RuntimeException e = new RuntimeException();
                        this.suppressed.forEach(e::addSuppressed);
                        TerraMod.LOGGER.error("Some URLs completed exceptionally", e);
                    }
                    this.future.complete(null);
                } else {
                    RuntimeException e = new RuntimeException("All URLs completed exceptionally!");
                    this.suppressed.forEach(e::addSuppressed);
                    this.future.completeExceptionally(e);
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
        Matcher matcher = URL_FORMATTING_MATCHER_CACHE.get().reset(url);
        if (matcher.find()) {
            StringBuffer out = new StringBuffer();
            do {
                String key = matcher.group(1);
                String value = properties.get(key);
                Preconditions.checkArgument(value != null, "unknown property: \"%s\"", key);
                matcher.appendReplacement(out, value);
            } while (matcher.find());
            matcher.appendTail(out);
            return out.toString();
        } else {
            return url;
        }
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
