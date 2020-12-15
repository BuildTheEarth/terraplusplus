package io.github.terra121.util.http;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
public class HttpTest {
    public static void main(String... args) {
        CompletableFuture<ByteBuf> future0 = Http.get("https://cloud.daporkchop.net/");
        CompletableFuture<ByteBuf> future1 = Http.get("https://cloud.daporkchop.net/misc/tobetote/weekly/9-30-shrunk.jpg");

        System.out.println(future0.join());
        System.out.println(future1.join());
    }
}
