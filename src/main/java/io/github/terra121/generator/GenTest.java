package io.github.terra121.generator;

import io.github.terra121.generator.cache.ChunkDataLoader;
import io.github.terra121.util.http.Http;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.ChunkPos;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.math.PMath.*;

//TODO: delete this before merge
public class GenTest {
    static final int SIZE = 1024;
    static final int BASE_CHUNK_X = (857742 >> 4);
    static final int BASE_CHUNK_Z = (4721747 >> 4) - 16;
    static final int SCALE = 0;

    static final int CHUNKS = SIZE >> 4;

    static ChunkDataLoader LOADER;

    public static void main(String... args) {
        Bootstrap.register();
        Http.configChanged();

        while (true) {
            doThing();
        }
    }

    private static void doThing() { //allows hot-swapping
        EarthGeneratorSettings cfg = new EarthGeneratorSettings("{\"projection\":\"equirectangular\",\"orentation\":\"swapped\",\"scaleX\":100000.0,\"scaleY\":100000.0,\"smoothblend\":true,\"roads\":false,\"customcubic\":\"\",\"dynamicbaseheight\":true,\"osmwater\":true,\"buildings\":false,\"caves\":false,\"lidar\":false,\"customdataset\":\"Custom Terrain Directory\"}");
        //cfg = new EarthGeneratorSettings("");
        LOADER = new ChunkDataLoader(new GeneratorDatasets(cfg.getProjection(), cfg, true));

        PorkUtil.simpleDisplayImage(true, tile(0, 0, SCALE)
                .thenApply(data -> {
                    BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

                    double minH = Arrays.stream(data[0]).min().getAsDouble();
                    double maxH = Arrays.stream(data[0]).max().getAsDouble();
                    double minW = Arrays.stream(data[1]).min().getAsDouble();
                    double maxW = Arrays.stream(data[1]).max().getAsDouble();

                    System.out.println(minW + " " + maxW);

                    for (int x = 0; x < SIZE; x++) {
                        for (int z = 0; z < SIZE; z++) {
                            int h = clamp(floorI((data[0][x * SIZE + z] - minH) * 255.0d / (maxH - minH)), 0, 255);
                            int w = clamp(floorI((data[1][x * SIZE + z] - minW) * 255.0d / (maxW - minW)), 0, 255);

                            img.setRGB(x, z, 0xFF000000 | h << 16 | w);
                        }
                    }
                    return img;
                })
                .join());
    }

    static CompletableFuture<double[][]> tile(int tileX, int tileZ, int level) {
        double[][] dst = new double[2][SIZE * SIZE];
        CompletableFuture[] futures;
        if (level == 0) {
            futures = new CompletableFuture[CHUNKS * CHUNKS];
            for (int i = 0, chunkX = 0; chunkX < CHUNKS; chunkX++) {
                for (int chunkZ = 0; chunkZ < CHUNKS; chunkZ++) {
                    int offX = chunkX << 4;
                    int offZ = chunkZ << 4;
                    futures[i++] = LOADER.load(new ChunkPos(BASE_CHUNK_Z + chunkX + tileX * CHUNKS, BASE_CHUNK_X + chunkZ + tileZ * CHUNKS))
                            .thenAccept(data -> {
                                for (int x = 0; x < 16; x++) {
                                    for (int z = 0; z < 16; z++) {
                                        int j = (offX + x) * SIZE + offZ + z;
                                        dst[0][j] = data.heights[x * 16 + z];
                                        dst[1][j] = data.wateroffs[x * 16 + z];
                                    }
                                }
                            });
                }
            }
        } else {
            futures = new CompletableFuture[4];
            for (int i = 0, dtx = 0; dtx < 2; dtx++) {
                for (int dtz = 0; dtz < 2; dtz++) {
                    int offX = (SIZE >> 1) * dtx;
                    int offZ = (SIZE >> 1) * dtz;
                    futures[i++] = tile((tileX << 1) + dtx, (tileZ << 1) + dtz, level - 1)
                            .thenAccept(data -> {
                                for (int x = 0; x < SIZE; x += 2) {
                                    for (int z = 0; z < SIZE; z += 2) {
                                        int j = (offX + (x >> 1)) * SIZE + offZ + (z >> 1);
                                        for (int l = 0; l < dst.length; l++) {
                                            double v = 0.0d;
                                            for (int dx = 0; dx < 2; dx++) {
                                                for (int dz = 0; dz < 2; dz++) {
                                                    v += data[l][(x + dx) * SIZE + (z + dz)];
                                                }
                                            }
                                            dst[l][j] = v * 0.25d;
                                        }
                                    }
                                }
                            });
                }
            }
        }
        return CompletableFuture.allOf(futures).thenApply(unused -> dst);
    }
}
