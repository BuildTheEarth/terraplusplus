package io.github.terra121.dataset;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.projection.transform.ScaleProjection;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.IntToDoubleBiFunction;
import io.github.terra121.util.bvh.Bounds2d;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.NonNull;
import net.daporkchop.lib.common.math.BinMath;
import net.daporkchop.lib.unsafe.PUnsafe;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static net.daporkchop.lib.common.util.PValidation.*;

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
        super(new ScaleProjection(proj, scale), 1.0d / scale * TILE_SIZE);

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

        return this.blendMode.get(projX, projZ, this);
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
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        if (notNegative(sizeX, "sizeX") == 0 | notNegative(sizeZ, "sizeZ") == 0) { //no input points -> no output points, ez
            return CompletableFuture.completedFuture(new double[0]);
        }

        CornerBoundingBox2d localBounds = bounds.fromGeo(this.projection);
        Bounds2d paddedLocalBounds = localBounds.axisAlign(-this.blendMode.size, this.blendMode.size);

        class State implements Function<Void, double[]>, IntToDoubleBiFunction {
            final Long2ObjectMap<double[]> loadedTiles = new Long2ObjectOpenHashMap<>();

            public CompletableFuture<Void> requestTiles() { //stage 1: compute positions of tiles to get and convert them to futures
                ChunkPos[] tilePositions = paddedLocalBounds.toTiles(TILE_SIZE);

                return CompletableFuture.allOf(Arrays.stream(tilePositions)
                        .map(pos -> DoubleTiledDataset.this.getTileAsync(pos)
                                .thenApply(tile -> { //put tile directly into map when it's loaded
                                    //synchronize because we can't be certain that all of the futures will be completed by the same thread
                                    synchronized (this.loadedTiles) {
                                        this.loadedTiles.put(BinMath.packXY(pos.x, pos.z), tile);
                                    }
                                    return tile;
                                }))
                        .toArray(CompletableFuture[]::new));
            }

            @Override
            public double[] apply(Void unused) { //stage 2: actually compute the values now that they've been fetched
                BlendMode blendMode = DoubleTiledDataset.this.blendMode;

                double stepX = 1.0d / sizeX;
                double stepZ = 1.0d / sizeZ;

                double[] point = new double[2];
                double[] out = new double[sizeX * sizeZ];

                double fx = 0.0d;
                for (int i = 0, x = 0; x < sizeX; x++, fx += stepX) {
                    double fz = 0.0d;
                    for (int z = 0; z < sizeZ; z++, fz += stepZ) {
                        //compute coordinates of point
                        point = localBounds.point(point, fx, fz);

                        //sample value at point
                        out[i++] = blendMode.get(point[0], point[1], this);
                    }
                }

                return out;
            }

            @Override
            public double apply(int x, int z) { //gets raw sample values to be used in blending
                double[] tile = this.loadedTiles.get(BinMath.packXY(x >> TILE_SHIFT, z >> TILE_SHIFT));
                checkArg(tile != null, "unknown tile (%d,%d)! sample: (%d,%d), local bounds: %s", x >> TILE_SHIFT, z >> TILE_SHIFT, x, z, localBounds);
                return tile[(z & TILE_MASK) * TILE_SIZE + (x & TILE_MASK)];
            }
        }

        State state = new State();
        return CompletableFuture.allOf(state.requestTiles()).thenApplyAsync(state);
    }
}
