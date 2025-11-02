package net.buildtheearth.terraminusminus.util.http;

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
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.daporkchop.lib.common.util.PorkUtil;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.buildtheearth.terraminusminus.util.http.Http.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Manages request queueing, connection keepalives and potential HTTP/2 upgrades for a single remote host.
 *
 * @author DaPorkchop_
 */
final class HostManager extends Host {
    private static final AttributeKey<Request> ATTR_REQUEST = AttributeKey.valueOf(Request.class, "terra--");

    private final Deque<Request> pendingRequests = new ArrayDeque<>();

    private final EventLoop eventLoop;
    private final Bootstrap bootstrap;

    private int maxConcurrentRequests = 1;
    private int activeRequests;

    private final Set<Channel> channels = Collections.newSetFromMap(new IdentityHashMap<>());
    private ChannelFuture channelFuture;

    public HostManager(@NonNull Host host) {
        super(host);

        this.eventLoop = NETWORK_EVENT_LOOP_GROUP.next();
        this.bootstrap = DEFAULT_BOOTSTRAP.clone()
                .group(this.eventLoop)
                .handler(new Initializer(new Handler()))
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
        for (Request request; this.activeRequests < this.maxConcurrentRequests && (request = this.pendingRequests.peek()) != null && this.trySendRequest0(request); ) {
            checkState(this.pendingRequests.poll() == request, "unable to remove request from queue!");
        }
    }

    private boolean trySendRequest0(@NonNull Request request) {
        if (request.callback.isCancelled()) { //future is already completed (probably due to cancellation), pretend that we handled it
            return true;
        }

        for (Channel channel : this.channels) {
            if (channel.attr(ATTR_REQUEST).compareAndSet(null, request)) { //the channel is currently inactive
                channel.pipeline().addFirst("read_timeout", new ReadTimeoutHandler(TIMEOUT, TimeUnit.SECONDS));
                channel.writeAndFlush(request.toNetty()); //send request
                this.activeRequests++;
                return true;
            }
        }

        this.considerOpeningAnotherConnection();
        return false;
    }

    private void considerOpeningAnotherConnection() {
        if (this.channelFuture == null) { //channelFuture is null, so there is no currently opening channel
            (this.channelFuture = this.bootstrap.connect()).addListener((ChannelFutureListener) this::handleChannelOpened);
        }
    }

    private void handleChannelOpened(@NonNull ChannelFuture channelFuture) {
        checkState(channelFuture == this.channelFuture, "unknown channel future?!?");
        this.channelFuture = null;

        if (!channelFuture.isSuccess()) {
            //TODO: fail pending requests only if no other connections are open
            this.pendingRequests.forEach(r -> r.callback.handle(null, channelFuture.cause()));
            this.pendingRequests.clear();
            return;
        }

        Channel channel = channelFuture.channel();
        this.channels.add(channel);
        channel.closeFuture().addListener((ChannelFutureListener) this::handleChannelClosed);

        this.tryWorkOffQueue();
    }

    private void handleChannelClosed(@NonNull ChannelFuture channelFuture) {
        Channel channel = channelFuture.channel();
        //if the channel is still stored as an active connection, it was closed for some other reason than the
        // server sending a "Connection: close" header, so let's double-check the channel state
        if (this.channels.remove(channel)) {
            Request request = channel.attr(ATTR_REQUEST).getAndSet(null);
            if (request != null) {
                //the channel still has a request associated with it! the channel was a keepalive channel,
                // and the server closed it at the same time as we sent the request. let's re-submit the request
                // so that it can be issued again on a new channel

                this.pendingRequests.addFirst(request); //add to front of queue so that it doesn't have to wait through the entire queue again
            }

            //working off the queue may open a new channel to replace this one if there are more pending requests
            this.tryWorkOffQueue();
        }
    }

    private void handleResponse(@NonNull Channel channel, Object msg) {
        Request request = null;
        try {
            if (!(msg instanceof FullHttpResponse)) {
                throw new IllegalArgumentException(PorkUtil.className(msg));
            }
            FullHttpResponse response = (FullHttpResponse) msg;
            if (response.status().codeClass() == HttpStatusClass.INFORMATIONAL) { //do nothing
                return;
            }

            request = channel.attr(ATTR_REQUEST).getAndSet(null);
            checkState(request != null, "received response on inactive channel?!?");

            this.activeRequests--; //decrement active requests counter to enable another request to be made

            if (!HttpUtil.isKeepAlive(response)) { //response isn't keep-alive, close connection
                //remove connection from active connections now to prevent it from
                // being re-used if the close operation isn't completed before this method ends
                this.channels.remove(channel);
                channel.close();
            }

            request.callback.handle(response, null);
        } catch (Exception e) {
            if (request != null) {
                request.callback.handle(null, e);
            }
        } finally {
            ReferenceCountUtil.release(msg);

            this.tryWorkOffQueue(); //if this request is completed, another slot must have been freed up
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
                    .set(HttpHeaderNames.USER_AGENT, Http.userAgent());
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

        @Override
        protected void initChannel(Channel ch) throws Exception {
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
            Request request = ctx.channel().attr(ATTR_REQUEST).getAndSet(null);
            if (request != null) { //inform request that it failed
                request.callback.handle(null, cause);
                HostManager.this.activeRequests--;
            }

            ctx.close();
        }
    }
}
