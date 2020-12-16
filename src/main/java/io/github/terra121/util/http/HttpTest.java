package io.github.terra121.util.http;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * @author DaPorkchop_
 */
public class HttpTest {
    public static void main(String... args) {
        System.out.println(CompletableFuture.allOf(IntStream.range(0, 16)
                .mapToObj(i -> String.format("https://cloud.daporkchop.net/gis/treecover2000/0/%d.tiff", i))
                .map(u -> Http.get(u).thenAccept(b -> System.out.println(u + ' ' + b)))
                .toArray(CompletableFuture[]::new)).join());
    }
}
