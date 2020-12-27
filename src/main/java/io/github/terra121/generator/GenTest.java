package io.github.terra121.generator;

import io.github.terra121.generator.cache.ChunkDataLoader;
import io.github.terra121.projection.MapsProjection;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.util.math.ChunkPos;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

//TODO: delete this before merge
public class GenTest {
    public static void main(String... args) {
        int size = 256;
        int baseChunkX = 4721747 >> 4;
        int baseChunkZ = 857742 >> 4;

        checkState((size & 0xF) == 0);

        ChunkDataLoader loader = new ChunkDataLoader(new GeneratorDatasets(new MapsProjection(), new EarthGeneratorSettings(""), true));
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        int chunks = size >> 4;
        CompletableFuture[] futures = new CompletableFuture[chunks * chunks];
        for (int i = 0, _chunkX = 0; _chunkX < chunks; _chunkX++) {
            for (int _chunkZ = 0; _chunkZ < chunks; _chunkZ++) {
                int chunkX = baseChunkX + _chunkX;
                int chunkZ = baseChunkZ + _chunkZ;
                futures[i++] = loader.load(new ChunkPos(chunkX, chunkZ))
                        .thenAccept(data -> {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    int h = clamp(floorI(data.heights[x * 16 + z] - 400.0d), 0, 255);
                                    int w = clamp(floorI(data.wateroffs[x * 16 + z] * 128.0d), 0, 255);

                                    int c = 0xFF000000 | h << 16 | w;
                                    img.setRGB(chunkX << 4 | x, chunkZ << 4 | z, c);
                                }
                            }
                        });
            }
        }
        CompletableFuture.allOf(futures).join();

        PorkUtil.simpleDisplayImage(true, img);
    }
}
