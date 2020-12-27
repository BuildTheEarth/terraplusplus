package io.github.terra121.generator;

import io.github.terra121.TerraMod;
import io.github.terra121.generator.cache.ChunkDataLoader;
import io.github.terra121.projection.MapsProjection;
import io.github.terra121.util.http.Http;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.ChunkPos;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

//TODO: delete this before merge
public class GenTest {
    public static void main(String... args) {
        TerraMod.LOGGER.info("mc init...");
        Bootstrap.register();
        Http.configChanged();

        int size = 1024;
        int baseChunkX = 857742 >> 4;
        int baseChunkZ = 4721747 >> 4;

        double minH = 500;
        double maxH = 700;

        checkState((size & 0xF) == 0);

        EarthGeneratorSettings cfg = new EarthGeneratorSettings("{\"projection\":\"equirectangular\",\"orentation\":\"swapped\",\"scaleX\":100000.0,\"scaleY\":100000.0,\"smoothblend\":true,\"roads\":true,\"customcubic\":\"\",\"dynamicbaseheight\":true,\"osmwater\":true,\"buildings\":true,\"caves\":false,\"lidar\":false,\"customdataset\":\"Custom Terrain Directory\"}");
        //cfg = new EarthGeneratorSettings("");
        ChunkDataLoader loader = new ChunkDataLoader(new GeneratorDatasets(cfg.getProjection(), cfg, true));
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        int chunks = size >> 4;
        CompletableFuture[] futures = new CompletableFuture[chunks * chunks];
        for (int i = 0, _chunkX = 0; _chunkX < chunks; _chunkX++) {
            for (int _chunkZ = 0; _chunkZ < chunks; _chunkZ++) {
                int chunkX = _chunkX;
                int chunkZ = _chunkZ;
                futures[i++] = loader.load(new ChunkPos(baseChunkZ + chunkX, baseChunkX + chunkZ))
                        .thenAccept(data -> {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    int h = clamp(floorI((data.heights[x * 16 + z] - minH) * 255.0d / (maxH - minH)), 0, 255);
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
