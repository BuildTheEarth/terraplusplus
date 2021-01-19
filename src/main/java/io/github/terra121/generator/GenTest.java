package io.github.terra121.generator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import io.github.terra121.BTEWorldType;
import io.github.terra121.generator.cache.CachedChunkData;
import io.github.terra121.generator.cache.ChunkDataLoader;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.http.Http;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.ChunkPos;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static net.daporkchop.lib.common.math.PMath.*;

//TODO: delete this before merge
public class GenTest {
    static final int SIZE = 512;
    static int BASE_CHUNK_X = 0;
    static int BASE_CHUNK_Z = 0;
    static int SCALE = -1;

    static final int CHUNKS = SIZE >> 4;

    static GeographicProjection PROJECTION;
    static Function<ChunkPos, CompletableFuture<CachedChunkData>> GET_FUNC;

    public static void main(String... args) throws OutOfProjectionBoundsException {
        Bootstrap.register();
        Http.configChanged();

        while (true) {
            doThing();
        }
    }

    private static void doThing() throws OutOfProjectionBoundsException { //allows hot-swapping
        Runnable initSettings = () -> {
            EarthGeneratorSettings cfg = new EarthGeneratorSettings(BTEWorldType.BTE_GENERATOR_SETTINGS);
            PROJECTION = cfg.getProjection();

            LoadingCache<ChunkPos, CompletableFuture<CachedChunkData>> cache = CacheBuilder.newBuilder()
                    .softValues()
                    .build(new ChunkDataLoader(new GeneratorDatasets(PROJECTION, cfg, true)));
            GET_FUNC = cache::getUnchecked;
        };
        initSettings.run();

        double[] proj = PROJECTION.fromGeo(8.57696, 47.21763);
        BASE_CHUNK_X = floorI(proj[0]) >> 4;
        BASE_CHUNK_Z = floorI(proj[1]) >> 4;

        JFrame frame = new JFrame();
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageIcon icon = new ImageIcon(img);
        JLabel label = new JLabel(icon);
        frame.getContentPane().add(label);

        Runnable updateImage = () -> {
            int off = floorI((CHUNKS >> 1) * Math.pow(2.0d, SCALE));
            BASE_CHUNK_X -= off;
            BASE_CHUNK_Z -= off;
            tile(0, 0, SCALE)
                    .thenApply(data -> {
                        double minH = Arrays.stream(data[0]).min().getAsDouble();
                        double maxH = Arrays.stream(data[0]).max().getAsDouble();
                        double minW = Arrays.stream(data[1]).min().getAsDouble();
                        double maxW = Arrays.stream(data[1]).max().getAsDouble();
                        double minB = Arrays.stream(data[2]).min().getAsDouble();
                        double maxB = Arrays.stream(data[2]).max().getAsDouble();

                        /*System.out.printf("H: %.4f %.4f\n", minH, maxH);
                        System.out.printf("W: %.4f %.4f\n", minW, maxW);
                        System.out.printf("B: %.4f %.4f\n", minB, maxB);*/

                        for (int x = 0; x < SIZE; x++) {
                            for (int z = 0; z < SIZE; z++) {
                                int h = clamp(floorI((data[0][x * SIZE + z] - minH) * 255.0d / (maxH - minH)), 0, 255);
                                int w = clamp(floorI((data[1][x * SIZE + z] - minW) * 255.0d / (maxW - minW)), 0, 255);
                                int b = clamp(floorI((data[2][x * SIZE + z] - minB) * 255.0d / (maxB - minB)), 0, 255);

                                img.setRGB(x, z, 0xFF000000 | h << 16 | w | b << 8);
                            }
                        }

                        icon.getImage().flush();
                        label.repaint();
                        return img;
                    })
                    .join();
            BASE_CHUNK_X += off;
            BASE_CHUNK_Z += off;
        };

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                        BASE_CHUNK_X += CHUNKS >> 2;
                        break;
                    case KeyEvent.VK_LEFT:
                        BASE_CHUNK_X -= CHUNKS >> 2;
                        break;
                    case KeyEvent.VK_UP:
                        BASE_CHUNK_Z -= CHUNKS >> 2;
                        break;
                    case KeyEvent.VK_DOWN:
                        BASE_CHUNK_Z += CHUNKS >> 2;
                        break;
                    case KeyEvent.VK_ADD:
                        SCALE--;
                        break;
                    case KeyEvent.VK_SUBTRACT:
                        SCALE++;
                        break;
                    case KeyEvent.VK_R:
                        initSettings.run();
                        break;
                }
                updateImage.run();
            }
        });

        updateImage.run();
        frame.pack();

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        CompletableFuture<Void> f = new CompletableFuture<>();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                f.complete(null);
            }
        });
        f.join();
    }

    static CompletableFuture<double[][]> tile(int tileX, int tileZ, int level) {
        double[][] dst = new double[3][SIZE * SIZE];
        CompletableFuture[] futures;
        if (level <= 0) {
            int d = 1 << -level;
            int s = -level;
            futures = new CompletableFuture[(CHUNKS >> -level) * (CHUNKS >> -level)];
            for (int i = 0, chunkX = 0; chunkX < CHUNKS >> -level; chunkX++) {
                for (int chunkZ = 0; chunkZ < CHUNKS >> -level; chunkZ++) {
                    int offX = chunkX << 4;
                    int offZ = chunkZ << 4;
                    futures[i++] = GET_FUNC.apply(new ChunkPos(BASE_CHUNK_X + chunkX + tileX * CHUNKS, BASE_CHUNK_Z + chunkZ + tileZ * CHUNKS))
                            .thenAccept(data -> {
                                for (int x = 0; x < 16; x++) {
                                    for (int z = 0; z < 16; z++) {
                                        for (int dx = 0; dx < d; dx++) {
                                            for (int dz = 0; dz < d; dz++) {
                                                int j = (((offX + x) << s) + dx) * SIZE + ((offZ + z) << s) + dz;
                                                dst[0][j] = data.groundHeight(x, z);
                                                dst[1][j] = data.surfaceHeight(x, z) - data.groundHeight(x, z);
                                                IBlockState state = data.surfaceBlocks().get(x * 16 + z);
                                                dst[2][j] = state == null ? 0.0d : state.getBlock() == Blocks.CONCRETE ? 1.0d : 2.0d;
                                            }
                                        }
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
