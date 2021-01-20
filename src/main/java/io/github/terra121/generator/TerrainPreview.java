package io.github.terra121.generator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.terra121.BTEWorldType;
import io.github.terra121.generator.cache.CachedChunkData;
import io.github.terra121.generator.cache.ChunkDataLoader;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.EmptyWorld;
import io.github.terra121.util.TilePos;
import io.github.terra121.util.http.Http;
import lombok.NonNull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
public class TerrainPreview extends CacheLoader<TilePos, CompletableFuture<BufferedImage>> {
    public static final int SIZE_SHIFT = 8;
    public static final int SIZE = 1 << SIZE_SHIFT; //resolution of a tile

    protected static final int CHUNKS_PER_TILE_SHIFT = SIZE_SHIFT - 4;
    protected static final int CHUNKS_PER_TILE = 1 << CHUNKS_PER_TILE_SHIFT; //number of chunks per tile at zoom 0

    public static BufferedImage createBlankTile() {
        return new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
    }

    public static void main(String... args) throws OutOfProjectionBoundsException {
        Bootstrap.register();
        Http.configChanged();

        while (true) {
            doThing();
        }
    }

    private static void doThing() throws OutOfProjectionBoundsException { //allows hot-swapping
        final int COUNT = 2;
        final int CANVAS_SIZE = SIZE * COUNT;

        class State extends JFrame implements KeyListener {
            final EarthGeneratorSettings settings;
            final GeographicProjection projection;
            TerrainPreview preview;

            final BufferedImage image = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_RGB);
            final Canvas canvas = new Canvas() {
                @Override
                public void paint(Graphics g) {
                    g.drawImage(State.this.image, 0, 0, null);
                }

                @Override
                public void update(Graphics g) {
                    this.paint(g);
                }
            };

            final int[] buffer = new int[SIZE * SIZE];

            int chunkX = 0;
            int chunkZ = 0;
            int zoom = 0;

            public State(@NonNull EarthGeneratorSettings settings) {
                this.settings = settings;
                this.projection = settings.getProjection();

                this.addKeyListener(this);
                this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                this.canvas.setBounds(0, 0, CANVAS_SIZE, CANVAS_SIZE);
                this.getContentPane().add(this.canvas);
            }

            public void setView(int chunkX, int chunkZ, int zoom) {
                this.chunkX = chunkX;
                this.chunkZ = chunkZ;
                this.zoom = zoom;
            }

            public void initSettings() {
                this.preview = new TerrainPreview(this.projection, this.settings);
            }

            public CompletableFuture<Void> run() {
                this.pack();
                this.setVisible(true);

                CompletableFuture<Void> f = new CompletableFuture<>();
                this.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        f.complete(null);
                    }
                });
                return f;
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int dx = 0;
                int dz = 0;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                        dx = 1;
                        break;
                    case KeyEvent.VK_LEFT:
                        dx = -1;
                        break;
                    case KeyEvent.VK_UP:
                        dz = -1;
                        break;
                    case KeyEvent.VK_DOWN:
                        dz = 1;
                        break;
                    case KeyEvent.VK_PAGE_UP:
                    case KeyEvent.VK_ADD:
                        this.zoom--;
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                    case KeyEvent.VK_SUBTRACT:
                        this.zoom++;
                        break;
                    case KeyEvent.VK_R:
                        this.initSettings();
                        break;
                }

                ORIENT:
                {
                    if (this.projection.upright()) {
                        if (this.settings.settings.orentation == GeographicProjection.Orientation.upright) {
                            break ORIENT;
                        }
                        dz = -dz;
                    }

                    if (this.settings.settings.orentation == GeographicProjection.Orientation.swapped) {
                        int i = dx;
                        dx = dz;
                        dz = i;
                    } else if (this.settings.settings.orentation == GeographicProjection.Orientation.upright) {
                        dz = -dz;
                    }
                }

                this.chunkX += dx * CHUNKS_PER_TILE;
                this.chunkZ += dz * CHUNKS_PER_TILE;

                this.update();
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            public void update() {
                CompletableFuture<Void>[] tileFutures = uncheckedCast(new CompletableFuture[COUNT * COUNT]);

                int zoom = this.zoom;
                int baseTX = zoom >= 0 ? this.chunkX >> (zoom + CHUNKS_PER_TILE_SHIFT) : 0;
                int baseTZ = zoom >= 0 ? this.chunkZ >> (zoom + CHUNKS_PER_TILE_SHIFT) : 0;

                for (int i = 0, _tx = 0; _tx < COUNT; _tx++) {
                    for (int _tz = 0; _tz < COUNT; _tz++) {
                        int tx = _tx;
                        int tz = _tz;
                        tileFutures[i++] = this.preview.tile(baseTX + tx, baseTZ + tz, this.zoom)
                                .thenAccept(tile -> {
                                    int baseX = tx << SIZE_SHIFT;
                                    int baseZ = tz << SIZE_SHIFT;
                                    synchronized (this.buffer) {
                                        tile.getRGB(0, 0, SIZE, SIZE, this.buffer, 0, SIZE);
                                        this.image.setRGB(baseX, baseZ, SIZE, SIZE, this.buffer, 0, SIZE);
                                    }
                                });
                    }
                }
                CompletableFuture.allOf(tileFutures).join();

                this.canvas.repaint();
            }
        }

        State state = new State(new EarthGeneratorSettings(BTEWorldType.BTE_GENERATOR_SETTINGS));
        state.initSettings();

        double[] proj = state.projection.fromGeo(8.57696d, 47.21763d);
        int off = (COUNT >> 1) * CHUNKS_PER_TILE;
        state.setView((floorI(proj[0]) >> 4) - off, (floorI(proj[1]) >> 4) - off, 0);

        state.update();

        state.run().join();
        state.dispose();
    }

    protected final LoadingCache<TilePos, CompletableFuture<BufferedImage>> cache;
    protected final ChunkDataLoader loader;

    public TerrainPreview(@NonNull EarthGeneratorSettings settings) {
        this(settings.getProjection(), settings);
    }

    public TerrainPreview(@NonNull GeographicProjection projection, @NonNull EarthGeneratorSettings settings) {
        this(new GeneratorDatasets(projection, settings));
    }

    public TerrainPreview(@NonNull GeneratorDatasets datasets) {
        this(datasets, "softValues");
    }

    public TerrainPreview(@NonNull GeneratorDatasets datasets, @NonNull String cacheSpec) {
        this.loader = new ChunkDataLoader(datasets);
        this.cache = CacheBuilder.from(cacheSpec).build(this);
    }

    public CompletableFuture<BufferedImage> tile(int x, int z, int zoom) {
        return this.tile(new TilePos(x, z, zoom));
    }

    public CompletableFuture<BufferedImage> tile(@NonNull TilePos pos) {
        return this.cache.getUnchecked(pos);
    }

    /**
     * @deprecated internal API, do not touch!
     */
    @Override
    @Deprecated
    public CompletableFuture<BufferedImage> load(@NonNull TilePos pos) {
        if (pos.zoom() == 0) {
            return this.baseZoomTile(pos.x(), pos.z());
        } else if (pos.zoom() > 0) {
            return this.zoomedOutTile(pos.x(), pos.z(), pos.zoom());
        } else {
            return this.zoomedInTile(pos.x(), pos.z(), pos.zoom());
        }
    }

    protected CompletableFuture<BufferedImage> baseZoomTile(int x, int z) {
        CompletableFuture<CachedChunkData>[] dataFutures = uncheckedCast(new CompletableFuture[CHUNKS_PER_TILE * CHUNKS_PER_TILE]);
        for (int i = 0, dx = 0; dx < CHUNKS_PER_TILE; dx++) {
            for (int dz = 0; dz < CHUNKS_PER_TILE; dz++) {
                dataFutures[i++] = this.loader.load(new ChunkPos((x << CHUNKS_PER_TILE_SHIFT) + dx, (z << CHUNKS_PER_TILE_SHIFT) + dz));
            }
        }

        return CompletableFuture.allOf(dataFutures).thenApply(unused -> {
            BufferedImage dst = createBlankTile();

            for (int ti = 0, tx = 0; tx < CHUNKS_PER_TILE; tx++) {
                for (int tz = 0; tz < CHUNKS_PER_TILE; tz++) {
                    CachedChunkData data = dataFutures[ti++].join();
                    int baseX = tx << 4;
                    int baseZ = tz << 4;
                    for (int cx = 0; cx < 16; cx++) {
                        for (int cz = 0; cz < 16; cz++) {
                            int c = 0;

                            IBlockState state = data.surfaceBlock(cx, cz);
                            if (state != null) {
                                c = state.getMapColor(EmptyWorld.INSTANCE, BlockPos.ORIGIN).colorValue;
                            }

                            dst.setRGB(baseX + cx, baseZ + cz, 0xFF000000 | c);
                        }
                    }
                }
            }

            return dst;
        });
    }

    protected CompletableFuture<BufferedImage> zoomedOutTile(int x, int z, int zoom) {
        CompletableFuture<BufferedImage>[] children = uncheckedCast(new CompletableFuture[4]);
        for (int i = 0, dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                children[i++] = this.tile((x << 1) | dx, (z << 1) | dz, zoom - 1);
            }
        }

        return CompletableFuture.allOf(children).thenApply(unused -> {
            BufferedImage dst = createBlankTile();
            int[] buf = new int[4];

            for (int ti = 0, tx = 0; tx < 2; tx++) {
                for (int tz = 0; tz < 2; tz++) {
                    BufferedImage child = children[ti++].join();
                    int baseX = tx << (SIZE_SHIFT - 1);
                    int baseZ = tz << (SIZE_SHIFT - 1);
                    for (int cx = 0; cx < SIZE >> 1; cx++) {
                        for (int cz = 0; cz < SIZE >> 1; cz++) {
                            child.getRGB(cx << 1, cz << 1, 2, 2, buf, 0, 2);

                            //compute average color
                            int r = 0;
                            int g = 0;
                            int b = 0;
                            for (int i = 0; i < 4; i++) {
                                int c = buf[i];
                                r += (c >> 16) & 0xFF;
                                g += (c >> 8) & 0xFF;
                                b += c & 0xFF;
                            }

                            dst.setRGB(baseX + cx, baseZ + cz, 0xFF000000 | ((r >> 2) << 16) | ((g >> 2) << 8) | (b >> 2));
                        }
                    }
                }
            }

            return dst;
        });
    }

    protected CompletableFuture<BufferedImage> zoomedInTile(int x, int z, int zoom) {
        return this.tile(x >> 1, z >> 1, zoom + 1)
                .thenApply(src -> {
                    BufferedImage dst = createBlankTile();
                    int[] buf = new int[4];

                    int baseX = (x & 1) << (SIZE_SHIFT >> 1);
                    int baseZ = (z & 1) << (SIZE_SHIFT >> 1);
                    for (int dx = 0; dx < SIZE >> 1; dx++) {
                        for (int dz = 0; dz < SIZE >> 1; dz++) {
                            Arrays.fill(buf, src.getRGB(baseX + dx, baseZ + dz));
                            dst.setRGB(dx << 1, dz << 1, 2, 2, buf, 0, 2);
                        }
                    }

                    return dst;
                });
    }
}
