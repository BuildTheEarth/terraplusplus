package io.github.terra121.util.http;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.PorkUtil;

import static io.github.terra121.util.http.Http.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class HttpChannelHandler extends ChannelDuplexHandler {
    @NonNull
    protected final HostManager manager;

    protected HostManager.Request request;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpResponse)) {
            throw new IllegalArgumentException(PorkUtil.className(msg));
        }

        ctx.channel().attr(ATTR_CHANNEL_FULL).set(false);
        HostManager.Request request = this.request;
        this.request = null;

        try {
            this.manager.handleRequestComplete(request, (FullHttpResponse) msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof HostManager.Request)) {
            throw new IllegalArgumentException(PorkUtil.className(msg));
        }

        HostManager.Request request = (HostManager.Request) msg;
        checkState(this.request == null, "duplicate request?!?");
        this.request = request;

        ctx.channel().attr(ATTR_CHANNEL_FULL).set(true);

        super.write(ctx, request.toNetty(HttpVersion.HTTP_1_1), promise);
    }
}
