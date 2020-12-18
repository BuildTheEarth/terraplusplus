package io.github.terra121.dataset;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.IntToDoubleBiFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.NonNull;
import net.daporkchop.lib.common.math.BinMath;

import java.util.BitSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * A {@link TiledDataset} which operates on a grid of interpolated {@code double}s.
 *
 * @author DaPorkchop_
 */
public abstract class DoubleTiledDataset extends TiledDataset<double[]> implements ScalarDataset, IntToDoubleBiFunction {
    protected static final int TILE_SHIFT = 8;
    protected static final int TILE_SIZE = 1 << TILE_SHIFT; //256
    protected static final int TILE_MASK = (1 << TILE_SHIFT) - 1; //0xFF

    protected static boolean isInRange(double lon, double lat) {
        return lon <= 180.0d && lon >= -180.0d && lat <= 85.0d && lat >= -85.0d;
    }

    public final BlendMode blendMode;

    public DoubleTiledDataset(GeographicProjection proj, double scale, @NonNull BlendMode blendMode) {
        super(proj, 1.0d / scale * TILE_SIZE, scale);

        this.blendMode = blendMode;
    }

    @Override
    public double get(double lon, double lat) {
        //basic bound check
        if (!isInRange(lon, lat)) {
            return -2.0d;
        }

        //project coords
        double projX;
        double projZ;
        try {
            double[] proj = this.projection.fromGeo(lon, lat);
            projX = proj[0];
            projZ = proj[1];
        } catch (OutOfProjectionBoundsException e) {
            return -2.0d;
        }

        return this.blendMode.get(projX * this.scale, projZ * this.scale, this);
    }

    /**
     * @deprecated internal API, do not touch!
     */
    @Deprecated
    @Override
    public double apply(int sampleX, int sampleZ) { //gets raw sample values to be used in blending
        if (sampleX <= this.minSampleX || sampleX >= this.maxSampleX) {
            return 0.0d;
        }

        double[] tileData = this.getTileAsync(sampleX >> TILE_SHIFT, sampleZ >> TILE_SHIFT).join();
        return tileData[(sampleZ & TILE_MASK) * TILE_SIZE + (sampleX & TILE_MASK)];
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull double[] lons, @NonNull double[] lats) {
        checkArg(lons.length == lats.length, "input coordinates must have same number of longitude values as latitudes!");
        int count = lons.length;

        if (count == 0) { //no input points -> no output points, ez
            return CompletableFuture.completedFuture(new double[0]);
        }

        class State implements Supplier<CompletableFuture<double[]>[]>, Function<Void, double[]>, IntToDoubleBiFunction {
            final BitSet oopbs = new BitSet(); //out of projection bounds
            double[] xs;
            double[] zs;
            double[] results;

            final Long2ObjectMap<double[]> loadedTiles = new Long2ObjectOpenHashMap<>();

            @Override
            public CompletableFuture<double[]>[] get() { //stage 1: compute positions of tiles to get
                this.xs = new double[count];
                this.zs = new double[count];
                this.results = new double[count];

                for (int i = 0; i < count; i++) {
                    if (isInRange(lons[i], lats[i])) {
                        try {
                            double[] proj = DoubleTiledDataset.this.projection.fromGeo(lons[i], lats[i]);
                            this.xs[i] = proj[0];
                            this.zs[i] = proj[1];
                            continue;
                        } catch (OutOfProjectionBoundsException e) {
                            //handled below
                        }
                    }

                    //the input coordinates are out of bounds, use error value and mark coordinate index as errored
                    this.results[i] = -2.0d;
                    this.oopbs.set(i);
                }

                Long2ObjectMap<CompletableFuture<double[]>> futures = new Long2ObjectOpenHashMap<>();

                double scale = DoubleTiledDataset.this.scale;
                BlendMode blendMode = DoubleTiledDataset.this.blendMode;
                double offset = blendMode.offset;
                int size = blendMode.size;

                for (int i = 0; i < count; i++) {
                    if (!this.oopbs.get(i)) { //skip input points that are out of projection bounds
                        int x = (int) floor(this.xs[i] * scale + offset);
                        int z = (int) floor(this.zs[i] * scale + offset);
                        for (int dx = 0; dx < size; dx++) { //expand to blending search radius
                            for (int dz = 0; dz < size; dz++) {
                                //also convert to tile coordinates
                                long pos = BinMath.packXY((x + dx) >> TILE_SHIFT, (z + dz) >> TILE_SHIFT);
                                if (!futures.containsKey(pos)) { //tile hasn't been requested yet, so issue the request now
                                    futures.put(pos, DoubleTiledDataset.this.getTileAsync(BinMath.unpackX(pos), BinMath.unpackY(pos))
                                            .thenApply(tile -> { //put tile directly into map when it's loaded
                                                synchronized (this.loadedTiles) { //synchronize because we can't be certain that all of the futures will be completed by a constant thread
                                                    this.loadedTiles.put(pos, tile);
                                                }
                                                return tile;
                                            }));
                                }
                            }
                        }
                    }
                }

                return uncheckedCast(futures.values().toArray(new CompletableFuture[0]));
            }

            @Override
            public double[] apply(Void unused) { //stage 2: actually compute the values now that they've been fetched
                for (int i = 0; i < count; i++) {
                    if (!this.oopbs.get(i)) { //skip input points that are out of projection bounds
                        this.results[i] = DoubleTiledDataset.this.blendMode.get(this.xs[i] * DoubleTiledDataset.this.scale, this.zs[i] * DoubleTiledDataset.this.scale, this);
                    }
                }
                return this.results;
            }

            @Override
            public double apply(int sampleX, int sampleZ) { //gets raw sample values to be used in blending
                double[] tile = this.loadedTiles.get(BinMath.packXY(sampleX >> TILE_SHIFT, sampleZ >> TILE_SHIFT));
                return tile[(sampleZ & TILE_MASK) * TILE_SIZE + (sampleX & TILE_MASK)];
            }
        }

        State state = new State();

        return CompletableFuture.supplyAsync(state)
                .<Void>thenCompose(CompletableFuture::allOf)
                .thenApplyAsync(state);
    }
}
