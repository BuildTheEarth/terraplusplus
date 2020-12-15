package io.github.terra121.util.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
final class HttpChannelInitializer extends ChannelInitializer<Channel> {
    @NonNull
    protected final HostManager manager;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (this.manager.ssl) {
            ch.pipeline().addLast(Http.SSL_CONTEXT.newHandler(ch.alloc(), this.manager.host, this.manager.port));
        }

        ch.pipeline().addLast(
                new HttpClientCodec(),
                new HttpContentDecompressor(),
                new HttpObjectAggregator(Integer.MAX_VALUE)); //unlikely, but whatever

        ch.pipeline().addLast(new HttpChannelHandler(this.manager));
    }
}
