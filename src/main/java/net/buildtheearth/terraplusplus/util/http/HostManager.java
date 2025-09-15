package net.buildtheearth.terraplusplus.util.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.daporkchop.lib.common.misc.string.PStrings;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.buildtheearth.terraplusplus.util.http.Http.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Manages request queueing, connection keepalives and potential HTTP/2 upgrades for a single remote host.
 *
 * @author DaPorkchop_
 */
final class HostManager extends Host {
    private static final boolean DEBUG_LOGGING = Boolean.getBoolean("terraplusplus.http.HostManager.debugLogging");

    private static final AttributeKey<Request> ATTR_REQUEST = AttributeKey.valueOf(Request.class, "terra++");

    private final Deque<Request> pendingRequests = new ArrayDeque<>();

    private final EventLoop eventLoop;
    private final Bootstrap bootstrap;

    private int maxConcurrentRequests = 1;

    final Set<Channel> allChannels = Collections.newSetFromMap(new IdentityHashMap<>());
    final Set<Channel> idleChannels = new ReferenceLinkedOpenHashSet<>(); //this set impl has fast iterator removal and regular removal

    private Future<?> connectFuture;

    public HostManager(@NonNull Host host) {
        super(host);

        this.eventLoop = NETWORK_EVENT_LOOP_GROUP.next();
        this.bootstrap = DEFAULT_BOOTSTRAP.clone()
                .group(this.eventLoop)
                .handler(new Initializer(new Handler(), this::handleChannelClosed))
                .remoteAddress(this.host, this.port)
                .attr(ATTR_REQUEST, null);
    }

    /**
     * Submits a GET request to this host.
     *
     * @param path     the path of the request
     * @param callback a {@link Callback} that will be notified once the request is completed
     */
    public void submit(@NonNull String path, @NonNull Callback callback, @NonNull HttpHeaders headers) {
        this.eventLoop.submit(() -> { //force execution on network thread
            this.pendingRequests.add(new Request(path, callback, headers)); //add to request queue

            this.tryWorkOffQueue();
        });
    }

    /**
     * Updates the maximum number of concurrent requests to this host.
     *
     * @param maxConcurrentRequests the new maximum number of concurrent requests
     */
    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = positive(maxConcurrentRequests, "maxConcurrentRequests");
    }

    private void tryWorkOffQueue() {
        assert this.eventLoop.inEventLoop() : Thread.currentThread();

        if (this.pendingRequests.isEmpty()) {
            //there are no requests in the queue, therefore there's nothing to do
            return;
        } else if (this.idleChannels.isEmpty()) {
            //there are no idle channels left, so we only have to try to open a new connection
            this.considerOpeningAnotherConnection();
            return;
        }

        Iterator<Channel> idleChannelIterator = this.idleChannels.iterator();
        while (true) {
            Request request = this.pendingRequests.peek();
            if (request == null) {
                //there are no pending requests, do nothing
                return;
            } else if (request.callback.isCancelled()) {
                //future is already completed (probably due to cancellation), remove it from the queue and proceed to the next request
                this.pendingRequests.poll();
                continue;
            }

            if (idleChannelIterator.hasNext()) {
                //send the request on this channel!
                Channel channel = idleChannelIterator.next();
                idleChannelIterator.remove();

                //we have just claimed a previously inactive channel for this request! we can remove the request from the queue and actually send it now.
                assert this.pendingRequests.peek() == request : "request isn't at the front of the queue anymore?!?";

                this.pendingRequests.poll();
                this.sendRequest(request, channel);

                //advance to the next request
                continue;
            }

            //there aren't any idle channels left!
            //we can try to open another connection if the parallelism limit hasn't been reached yet, but we have to stop afterwards since the request will
            //  have to wait in the queue until the connection is opened.
            this.considerOpeningAnotherConnection();
            return;
        }
    }

    private void addIdleChannel(@NonNull Channel channel) {
        assert this.eventLoop.inEventLoop() : Thread.currentThread();

        while (true) {
            Request request = this.pendingRequests.poll();
            if (request == null) {
                //there are no remaining requests in the queue, just add the channel to the idle set and return
                this.idleChannels.add(channel);
                return;
            } else if (request.callback.isCancelled()) {
                //future is already completed (probably due to cancellation), remove it from the queue and proceed to the next request
                continue;
            }

            //immediately send the next request on the channel without touching the idle set
            this.sendRequest(request, channel);

            //if there are still requests left we should try to push them out to waiting connections
            if (!this.pendingRequests.isEmpty()) {
                this.tryWorkOffQueue();
            }
            return;
        }
    }

    private void sendRequest(@NonNull Request request, @NonNull Channel channel) {
        checkState(channel.attr(ATTR_REQUEST).compareAndSet(null, request), "channel already has an associated request?!?");

        //add read timeout handler to the front of the pipeline so that we can time out the request if the response takes too long to arrive
        channel.pipeline().addFirst("read_timeout", new ReadTimeoutHandler(TIMEOUT, TimeUnit.SECONDS));

        //send the actual http request to the server
        channel.writeAndFlush(request.toNetty());
    }

    private void considerOpeningAnotherConnection() {
        assert this.eventLoop.inEventLoop() : Thread.currentThread();

        if (this.allChannels.size() >= this.maxConcurrentRequests) {
            //refuse to open more connections than the limit
            return;
        }

        if (this.connectFuture == null) { //channelFuture is null, so there is no currently opening channel
            ChannelFuture connectFuture = this.bootstrap.connect();
            this.connectFuture = connectFuture;
            connectFuture.addListener((ChannelFutureListener) this::handleChannelConnected);
        }
    }

    private void handleChannelConnected(@NonNull ChannelFuture channelFuture) {
        assert this.eventLoop.inEventLoop() : Thread.currentThread();

        checkState(channelFuture == this.connectFuture, "unexpected channel future?!?");
        this.connectFuture = null;

        if (!channelFuture.isSuccess()) {
            this.handleConnectionFailed(channelFuture.channel(), channelFuture.cause());
            return;
        }

        Channel channel = channelFuture.channel();

        if (this.ssl) {
            //if SSL is enabled we should wait for the SSL handshake to complete before treating the channel as ready
            this.connectFuture = channel.pipeline().get(SslHandler.class).handshakeFuture();
            this.connectFuture.addListener(handshakeFuture -> {
                synchronized (this) {
                    checkState(handshakeFuture == this.connectFuture, "unexpected handshake future?!?");
                    this.connectFuture = null;

                    if (!handshakeFuture.isSuccess()) {
                        this.handleConnectionFailed(channel, handshakeFuture.cause());
                        return;
                    }

                    //this channel is now ready to go, mark it as idle and then try to submit a request on it
                    this.addIdleChannel(channel);
                }
            });
        } else {
            //this channel is now ready to go, mark it as idle and then try to submit a request on it
            this.addIdleChannel(channel);
        }
    }

    private void handleConnectionFailed(@NonNull Channel channel, @NonNull Throwable cause) {
        assert this.eventLoop.inEventLoop() : Thread.currentThread();

        try {
            //TODO: fail pending requests only if no other connections are open
            this.pendingRequests.forEach(r -> r.callback.handle(null, cause));
            this.pendingRequests.clear();
        } finally {
            //make sure the channel is closed
            channel.close();
        }
    }

    private void handleChannelClosed(@NonNull ChannelFuture channelFuture) {
        Channel channel = channelFuture.channel();

        this.allChannels.remove(channel);
        this.idleChannels.remove(channel);

        Request request = channel.attr(ATTR_REQUEST).getAndSet(null);
        if (request != null) {
            //if the channel still has an associated request, it was closed before receiving a response
            // but without triggering an exception. most likely the channel was a keepalive channel,
            // and the server closed it at the same time as we sent the request. let's re-submit the request
            // so that it can be issued again on a new channel
            this.pendingRequests.addFirst(request); //add to front of queue so that it doesn't have to wait through the entire queue again
        }

        //working off the queue may open a new channel to replace this one if there are more pending requests
        this.tryWorkOffQueue();
    }

    private void handleResponse(@NonNull Channel channel, Object msg) {
        //any exceptions thrown here will cause the channel to be closed
        try {
            FullHttpResponse response = (FullHttpResponse) msg;
            if (response.status().codeClass() == HttpStatusClass.INFORMATIONAL) { //do nothing
                return;
            }

            Request request = channel.attr(ATTR_REQUEST).getAndSet(null);
            checkState(request != null, "received response on inactive channel?!?");

            request.callback.handle(response, null);

            if (!HttpUtil.isKeepAlive(response)) {
                //response has "Connection: close" header, so we should close the connection
                channel.close();
            } else if (this.allChannels.size() > this.maxConcurrentRequests) {
                //maxConcurrentRequests has been reduced and now there are too many connections open, close this channel
                channel.close();
            } else {
                //the channel is idle again now that it's received a response
                this.addIdleChannel(channel);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * A callback function that is executed when the request is completed.
     *
     * @author DaPorkchop_
     */
    public interface Callback {
        /**
         * @return whether or not the request has been cancelled
         */
        boolean isCancelled();

        /**
         * Handles the response body.
         *
         * @param response  the HTTP response
         * @param throwable the {@link Throwable} that was thrown (if the request was not able to be executed successfully)
         */
        void handle(FullHttpResponse response, Throwable throwable);
    }

    /**
     * A queued request.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    @ToString
    private final class Request {
        @NonNull
        protected final String path;
        @NonNull
        protected final Callback callback;
        @NonNull
        protected final HttpHeaders headers;

        public HttpRequest toNetty() {
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, this.path);
            request.headers()
                    .set(this.headers)
                    .set(HttpHeaderNames.HOST, HostManager.this.authority)
                    .set(HttpHeaderNames.USER_AGENT, PStrings.fastFormat("%s/%s CubicChunks/%s", TerraConstants.MODID, TerraConstants.VERSION, TerraConstants.CC_VERSION))
                    .set(HttpHeaderNames.ACCEPT_ENCODING, "gzip, deflate");
            HttpUtil.setKeepAlive(request, true);
            return request;
        }
    }

    /**
     * Initializes a {@link Channel} for sending HTTP(S) requests.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    private final class Initializer extends ChannelInitializer<Channel> {
        @NonNull
        private final ChannelHandler httpHandler;

        private final @NonNull ChannelFutureListener closeChannelListener;

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.closeFuture().addListener(this.closeChannelListener);

            HostManager.this.allChannels.add(ch);

            ch.pipeline().addLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.SECONDS));

            if (HostManager.this.ssl) {
                ch.pipeline().addLast(Http.SSL_CONTEXT.newHandler(ch.alloc(), HostManager.this.host, HostManager.this.port));
            }

            ch.pipeline().addLast(
                    new HttpClientCodec(),
                    new HttpContentDecompressor(),
                    new HttpObjectAggregator(Http.MAX_CONTENT_LENGTH),
                    this.httpHandler);
        }
    }

    /**
     * Relays messages that reach the tail of the Netty pipeline to the host manager.
     *
     * @author DaPorkchop_
     */
    @ChannelHandler.Sharable
    private final class Handler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ctx.pipeline().remove("read_timeout"); //remove read timeout listener to prevent a fake timeout if the connection is idle
            HostManager.this.handleResponse(ctx.channel(), msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            try {
                Request request = ctx.channel().attr(ATTR_REQUEST).getAndSet(null);
                if (request != null) { //inform request that it failed
                    request.callback.handle(null, cause);
                }
            } finally {
                //always close the channel if an exception occurs, we won't try to recover from this
                ctx.close();
            }
        }
    }
}
