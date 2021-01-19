package io.github.terra121.generator;

import io.github.terra121.BTEWorldType;
import io.github.terra121.generator.cache.ChunkDataLoader;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.http.Http;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.ChunkPos;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.math.PMath.*;

//TODO: delete this before merge
public class GenTest {
    static final int SIZE = 1024;
    static int BASE_CHUNK_X = 0;
    static int BASE_CHUNK_Z = 0;
    static final int SCALE = 1;

    static final int CHUNKS = SIZE >> 4;

    static ChunkDataLoader LOADER;

    public static void main(String... args) throws OutOfProjectionBoundsException {
        Bootstrap.register();
        Http.configChanged();

        while (true) {
            doThing();
        }
    }

    private static void doThing() throws OutOfProjectionBoundsException { //allows hot-swapping
        EarthGeneratorSettings cfg = new EarthGeneratorSettings("{\"projection\":\"equirectangular\",\"orentation\":\"upright\",\"scaleX\":100000.0,\"scaleY\":100000.0,\"smoothblend\":true,\"roads\":false,\"customcubic\":\"\",\"dynamicbaseheight\":true,\"osmwater\":true,\"buildings\":false,\"caves\":false,\"lidar\":false,\"customdataset\":\"Custom Terrain Directory\"}");
        cfg = new EarthGeneratorSettings(BTEWorldType.BTE_GENERATOR_SETTINGS);
        LOADER = new ChunkDataLoader(new GeneratorDatasets(cfg.getProjection(), cfg, true));

        double[] proj = cfg.getProjection().fromGeo(8.57696, 47.21763);
        BASE_CHUNK_X = (floorI(proj[0]) >> 4) - (CHUNKS >> 1);
        BASE_CHUNK_Z = (floorI(proj[1]) >> 4) - (CHUNKS >> 1);

        PorkUtil.simpleDisplayImage(true, tile(0, 0, SCALE)
                .thenApply(data -> {
                    System.out.println(Arrays.toString(proj));

                    BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

                    double minH = Arrays.stream(data[0]).min().getAsDouble();
                    double maxH = Arrays.stream(data[0]).max().getAsDouble();
                    double minW = Arrays.stream(data[1]).min().getAsDouble();
                    double maxW = Arrays.stream(data[1]).max().getAsDouble();
                    double minB = Arrays.stream(data[2]).min().getAsDouble();
                    double maxB = Arrays.stream(data[2]).max().getAsDouble();

                    System.out.printf("H: %.4f %.4f\n", minH, maxH);
                    System.out.printf("W: %.4f %.4f\n", minW, maxW);
                    System.out.printf("B: %.4f %.4f\n", minB, maxB);

                    for (int x = 0; x < SIZE; x++) {
                        for (int z = 0; z < SIZE; z++) {
                            int h = clamp(floorI((data[0][x * SIZE + z] - minH) * 255.0d / (maxH - minH)), 0, 255);
                            int w = clamp(floorI((data[1][x * SIZE + z] - minW) * 255.0d / (maxW - minW)), 0, 255);
                            int b = clamp(floorI((data[2][x * SIZE + z] - minB) * 255.0d / (maxB - minB)), 0, 255);

                            img.setRGB(x, z, 0xFF000000 | h << 16 | w | b << 8);
                        }
                    }
                    return img;
                })
                .join());
    }

    static CompletableFuture<double[][]> tile(int tileX, int tileZ, int level) {
        double[][] dst = new double[3][SIZE * SIZE];
        CompletableFuture[] futures;
        if (level == 0) {
            futures = new CompletableFuture[CHUNKS * CHUNKS];
            for (int i = 0, chunkX = 0; chunkX < CHUNKS; chunkX++) {
                for (int chunkZ = 0; chunkZ < CHUNKS; chunkZ++) {
                    int offX = chunkX << 4;
                    int offZ = chunkZ << 4;
                    futures[i++] = LOADER.load(new ChunkPos(BASE_CHUNK_X + chunkX + tileX * CHUNKS, BASE_CHUNK_Z + chunkZ + tileZ * CHUNKS))
                            .thenAccept(data -> {
                                for (int x = 0; x < 16; x++) {
                                    for (int z = 0; z < 16; z++) {
                                        int j = (offX + x) * SIZE + offZ + z;
                                        dst[0][j] = data.groundHeight(x, z);
                                        dst[1][j] = data.surfaceHeight(x, z) - data.groundHeight(x, z);
                                        IBlockState state = data.surfaceBlocks().get(x * 16 + z);
                                        dst[2][j] = state == null ? 0.0d : state.getBlock() == Blocks.CONCRETE ? 1.0d : 2.0d;
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
                                synchronized (dst) {
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
                                }
                            });
                }
            }
        }
        return CompletableFuture.allOf(futures).whenComplete((v, t) -> {}).thenApply(unused -> dst);
    }
}
