package io.github.terra121.util.http;

import io.netty.buffer.ByteBuf;
import net.daporkchop.lib.common.util.PorkUtil;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * @author DaPorkchop_
 */
public class HttpTest {
    public static void main(String... args) {
        CompletableFuture[] futures = IntStream.range(0, 16)
                .mapToObj(i -> String.format("https://cloud.daporkchop.net/gis/treecover2000/0/%d.tiff", i))
                .map(u -> {
                    CompletableFuture<ByteBuf> f = Http.get(u);
                    f.thenAccept(b -> System.out.println(u + ' ' + b));
                    return f;
                })
                .toArray(CompletableFuture[]::new);

        CompletableFuture all = CompletableFuture.allOf(futures);

        for (int i = 2; i < futures.length; i++) {
            futures[i].cancel(true);
        }

        try {
            all.get();
        } catch (Exception ignored) {
        }
    }
}
