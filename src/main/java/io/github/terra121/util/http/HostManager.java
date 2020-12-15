package io.github.terra121.util.http;

import io.github.terra121.TerraMod;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static io.github.terra121.util.http.Http.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Manages request queueing, connection keepalives and potential HTTP/2 upgrades for a single remote host.
 *
 * @author DaPorkchop_
 */
final class HostManager extends Host {
    protected final Queue<Request> pendingRequests = new ArrayDeque<>();
    protected final Set<Request> activeRequests = Collections.newSetFromMap(new IdentityHashMap<>());

    protected final int maxConcurrentRequests;

    protected Channel channel = null;
    protected ChannelFuture channelFuture = null;

    public HostManager(@NonNull URL url, int maxConcurrentRequests) {
        super(url);

        this.maxConcurrentRequests = positive(maxConcurrentRequests, "maxConcurrentRequests");
    }

    public HostManager(@NonNull Host host, int maxConcurrentRequests) {
        super(host.host, host.port, host.ssl);

        this.maxConcurrentRequests = positive(maxConcurrentRequests, "maxConcurrentRequests");
    }

    /**
     * Submits a GET request to this host.
     *
     * @param path   the path of the request
     * @param future a {@link CompletableFuture} that will be notified once the request is completed
     */
    public void submit(@NonNull String path, @NonNull CompletableFuture<ByteBuf> future) {
        if (!NETWORK_EVENT_LOOP.inEventLoop()) { //execute on network thread
            NETWORK_EVENT_LOOP.submit(() -> this.submit(path, future));
            return;
        }

        this.submit0(new Request(this.host, path, future));
    }

    private synchronized void submit0(@NonNull Request request) {
        if (this.activeRequests.size() >= this.maxConcurrentRequests) { //add to request queue and exit
            this.pendingRequests.add(request);
            return;
        }

        if (!this.trySendRequest0(request)) { //no currently open channels
            this.considerOpeningAnotherConnection();

            //add the request to the queue so that it can be sent once the channel is open
            this.pendingRequests.add(request);
        }
    }

    private synchronized boolean trySendRequest0(@NonNull Request request) {
        if (this.channel == null || this.channel.attr(ATTR_CHANNEL_FULL).get()) {
            return false;
        }

        this.activeRequests.add(request); //mark request as active
        this.channel.writeAndFlush(request, this.channel.voidPromise()); //send request
        return true;
    }

    private synchronized void considerOpeningAnotherConnection() {
        if (this.channel == null && this.channelFuture == null) {
            //open a new connection
            this.channelFuture = Http.DEFAULT_BOOTSTRAP.clone()
                    .handler(new HttpChannelInitializer(this))
                    .connect(this.host, this.port)
                    .addListener((ChannelFutureListener) this::handleChannelOpened);
        }
    }

    private synchronized void handleChannelOpened(@NonNull ChannelFuture channelFuture) {
        checkState(channelFuture == this.channelFuture, "unknown channel future?!?");
        this.channelFuture = null;

        if (!channelFuture.isSuccess()) {
            //TODO: fail pending requests only if no other connections are open
            this.pendingRequests.forEach(r -> r.future.completeExceptionally(channelFuture.cause()));
            this.pendingRequests.clear();
            return;
        }

        (this.channel = channelFuture.channel()).closeFuture().addListener((ChannelFutureListener) this::handleChannelClosed);

        Request request = this.pendingRequests.peek();
        if (request != null && this.trySendRequest0(request)) { //send request
            checkState(this.pendingRequests.poll() == request, "unable to remove request from queue!");
        }
    }

    private synchronized void handleChannelClosed(@NonNull ChannelFuture channelFuture) {
        System.out.println("channel closed: " + channelFuture.channel());
    }

    synchronized void handleRequestComplete(@NonNull Request request, @NonNull FullHttpResponse response) {
        switch (response.status().codeClass()) {
            case SUCCESS: //notify handler
                request.future.complete(response.content().copy());
                break;
            case INFORMATIONAL: //no-op
                break;
            case REDIRECTION: //handle redirect safely
                try {
                    URL url = new URL(response.headers().get(HttpHeaderNames.LOCATION));
                    Http.managerFor(url).submit(url.getFile(), request.future);
                } catch (MalformedURLException e) {
                    request.future.completeExceptionally(e);
                }
                break;
            default: //failure
                request.future.completeExceptionally(new IOException("response from server: " + response.status()));
        }

        if ((request = this.pendingRequests.peek()) != null && this.trySendRequest0(request)) { //send request
            checkState(this.pendingRequests.poll() == request, "unable to remove request from queue!");
        }
    }

    /**
     * A queued request.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    static final class Request {
        @NonNull
        protected final String host;
        @NonNull
        protected final String path;
        @NonNull
        protected final CompletableFuture<ByteBuf> future;

        public HttpRequest toNetty(@NonNull HttpVersion version) {
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(version, HttpMethod.GET, this.path);
            request.headers()
                    .set(HttpHeaderNames.HOST, this.host)
                    .set(HttpHeaderNames.USER_AGENT, TerraMod.USERAGENT);
            return request;
        }
    }
}
