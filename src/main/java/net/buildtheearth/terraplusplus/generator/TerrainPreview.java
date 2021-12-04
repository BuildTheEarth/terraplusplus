package net.buildtheearth.terraplusplus.generator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.data.TreeCoverBaker;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.EmptyWorld;
import net.buildtheearth.terraplusplus.util.TilePos;
import net.buildtheearth.terraplusplus.util.http.Http;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.client.model.pipeline.LightUtil;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static java.lang.Math.*;
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
        final int COUNT = 5;
        final int SHIFT = 1;
        final int CANVAS_SIZE = SIZE * COUNT;

        class State extends JFrame implements KeyEventDispatcher {
            final EarthGeneratorSettings settings;
            final GeographicProjection projection;
            TerrainPreview preview;

            final BufferedImage image = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_RGB);
            final Canvas canvas = new Canvas() {
                @Override
                public void paint(Graphics g) {
                    ((Graphics2D) g).addRenderingHints(Collections.singletonMap(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
                    g.drawImage(State.this.image, 0, 0, CANVAS_SIZE >> SHIFT, CANVAS_SIZE >> SHIFT, null);
                }

                @Override
                public void update(Graphics g) {
                    this.paint(g);
                }
            };

            final int[] buffer = new int[SIZE * SIZE];

            int chunkX;
            int chunkZ;
            int zoom;

            public State(@NonNull EarthGeneratorSettings settings) {
                this.settings = settings;
                this.projection = settings.projection();

                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
                this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                this.canvas.setBounds(0, 0, CANVAS_SIZE >> SHIFT, CANVAS_SIZE >> SHIFT);
                this.getContentPane().add(this.canvas);
            }

            public void setView(int chunkX, int chunkZ, int zoom) {
                this.chunkX = chunkX;
                this.chunkZ = chunkZ;
                this.zoom = zoom;
            }

            public void initSettings() {
                this.preview = new TerrainPreview(this.settings);
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
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() != KeyEvent.KEY_PRESSED) {
                    return false;
                }

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

                int scale = this.zoom >= 0 ? CHUNKS_PER_TILE << this.zoom : CHUNKS_PER_TILE >> -this.zoom;
                this.chunkX += dx * scale;
                this.chunkZ += dz * scale;

                this.update();
                return true;
            }

            public void update() {
                CompletableFuture<Void>[] tileFutures = uncheckedCast(new CompletableFuture[COUNT * COUNT]);

                int zoom = this.zoom;
                int baseTX = ((zoom >= 0 ? this.chunkX >> zoom : this.chunkX << -zoom) >> CHUNKS_PER_TILE_SHIFT) - (COUNT >> 1);
                int baseTZ = ((zoom >= 0 ? this.chunkZ >> zoom : this.chunkZ << -zoom) >> CHUNKS_PER_TILE_SHIFT) - (COUNT >> 1);

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

            @Override
            public void dispose() {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                super.dispose();
            }
        }

        State state = new State(EarthGeneratorSettings.parseUncached(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS));
        state.initSettings();

        double[] proj = new double[2]; //null island
        //proj = state.projection.fromGeo(8.57696d, 47.21763d); //steinhausen, switzerland
        //proj = state.projection.fromGeo(12.58589, 55.68841); //copenhagen, denmark
        //proj = state.projection.fromGeo(24.7535, 59.4435); //tallinn, estonia
        //proj = state.projection.fromGeo(14.50513, 46.05108); //ljubljana, slovenia
        //proj = state.projection.fromGeo(2.29118, 48.86020); //paris, france
        //proj = state.projection.fromGeo(-9.42956, 52.97183); //cliffs of moher, ireland
        //proj = state.projection.fromGeo(9.70089, 39.92472); //tortoli, italy
        //proj = state.projection.fromGeo(15.085464455006724, 37.50954065726297); //somewhere in sicily
        //proj = state.projection.fromGeo(12.610463237424899, 37.673937184583636); //somewhere in sicily
        //proj = state.projection.fromGeo(9.6726, 45.6699); //lombardia, italy
        //proj = state.projection.fromGeo(8.93058, 44.40804); //genova, italy
        //proj = state.projection.fromGeo(16.5922, 38.9069); //catanzaro, italy
        //proj = state.projection.fromGeo(-3.7070, 40.4168); //madrid, spain
        //proj = state.projection.fromGeo(-5.57589, 37.47938); //middle of nowhere, spain
        //proj = state.projection.fromGeo(13.37156, 52.52360); //berlin, germany
        //proj = state.projection.fromGeo(11.63779, 52.11903); //magdeburg, germany
        //proj = state.projection.fromGeo(7.206603551122279, 50.66019804133367); //röhndorf, germany
        //proj = state.projection.fromGeo(12.35027, 51.33524); //leipzig, germany
        //proj = state.projection.fromGeo(14.80963, 50.88887); //zittau, germany
        //proj = state.projection.fromGeo(-6.25900, 53.34702); //dublin, ireland
        proj = state.projection.fromGeo(5.33831, 50.22487); //marche-en-famenne, belgium
        state.setView(floorI(proj[0]) >> 4, floorI(proj[1]) >> 4, 0);

        state.update();

        state.run().join();
        state.dispose();

        System.gc();
    }

    protected final LoadingCache<TilePos, CompletableFuture<BufferedImage>> cache;
    protected final EarthGenerator.ChunkDataLoader loader;

    public TerrainPreview(@NonNull EarthGeneratorSettings settings) {
        this(settings, "softValues");
    }

    public TerrainPreview(@NonNull EarthGeneratorSettings settings, @NonNull String cacheSpec) {
        this.loader = new EarthGenerator.ChunkDataLoader(settings);
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

        return CompletableFuture.allOf(dataFutures).thenApplyAsync(unused -> {
            BufferedImage dst = createBlankTile();

            for (int ti = 0, tx = 0; tx < CHUNKS_PER_TILE; tx++) {
                for (int tz = 0; tz < CHUNKS_PER_TILE; tz++) {
                    CachedChunkData data = dataFutures[ti++].join();

                    byte[] treeCoverArr = data.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, TreeCoverBaker.FALLBACK_TREE_DENSITY);

                    int baseX = tx << 4;
                    int baseZ = tz << 4;
                    for (int cx = 0; cx < 16; cx++) {
                        for (int cz = 0; cz < 16; cz++) {
                            int c;

                            IBlockState state = data.surfaceBlock(cx, cz);
                            if (state != null) {
                                c = state.getMapColor(EmptyWorld.INSTANCE, BlockPos.ORIGIN).colorValue;
                            } else {
                                int groundHeight = data.groundHeight(cx, cz);
                                int waterHeight = data.waterHeight(cx, cz);

                                int r;
                                int g;
                                int b;

                                if (true || groundHeight > waterHeight) {
                                    float dx = cx == 15 ? groundHeight - data.groundHeight(cx - 1, cz) : data.groundHeight(cx + 1, cz) - groundHeight;
                                    float dz = cz == 15 ? groundHeight - data.groundHeight(cx, cz - 1) : data.groundHeight(cx, cz + 1) - groundHeight;
                                    int diffuse = floorI(LightUtil.diffuseLight(clamp(dx, -1.0f, 1.0f), 0.0f, clamp(dz, -1.0f, 1.0f)) * 255.0f);
                                    r = g = b = diffuse;

                                    if (groundHeight <= waterHeight) {
                                        r >>= 1;
                                        g >>= 1;
                                    }
                                } else {
                                    r = g = 0;
                                    b = lerpI(255, 64, clamp(waterHeight - groundHeight + 1, 0, 8) / 8.0f);
                                }

                                g = max(g, lerpI(0, 80, (treeCoverArr[cx * 16 + cz] & 0xFF) * TreeCoverBaker.TREE_AREA * (1.0d / 255.0d)));
                                c = r << 16 | g << 8 | b;
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

        return CompletableFuture.allOf(children).thenApplyAsync(unused -> {
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
                .thenApplyAsync(src -> {
                    BufferedImage dst = createBlankTile();
                    int[] buf = new int[4];

                    int baseX = (x & 1) << (SIZE_SHIFT - 1);
                    int baseZ = (z & 1) << (SIZE_SHIFT - 1);
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
