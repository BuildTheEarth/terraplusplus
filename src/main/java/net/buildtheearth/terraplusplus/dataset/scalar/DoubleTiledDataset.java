package net.buildtheearth.terraplusplus.dataset.scalar;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.dataset.BlendMode;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.dataset.TiledDataset;
import net.buildtheearth.terraplusplus.dataset.TiledHttpDataset;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.IntToDoubleBiFunction;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.daporkchop.lib.common.math.BinMath;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A {@link TiledDataset} which operates on a grid of interpolated {@code double}s.
 *
 * @author DaPorkchop_
 */
@Getter
public abstract class DoubleTiledDataset extends TiledHttpDataset<double[]> implements IScalarDataset {
    protected static final int TILE_SHIFT = 8;
    protected static final int TILE_SIZE = 1 << TILE_SHIFT; //256
    protected static final int TILE_MASK = (1 << TILE_SHIFT) - 1; //0xFF

    protected final BlendMode blend;
    protected final int resolution;

    protected final double[] degreesPerSample;

    public DoubleTiledDataset(@NonNull GeographicProjection projection, int resolution, @NonNull BlendMode blend) {
        super(projection, 1.0d / resolution);
        checkArg(resolution == 256, "unsupported resolution: %d (expected: 256)", resolution);

        this.resolution = resolution;
        this.blend = blend;

        double[] bounds = this.projection().bounds();
        double[] boundsGeo = this.projection().boundsGeo();
        this.degreesPerSample = new double[]{
                abs(boundsGeo[2] - boundsGeo[0]) / abs(bounds[2] - bounds[0]),
                abs(boundsGeo[3] - boundsGeo[1]) / abs(bounds[3] - bounds[1])
        };
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        class State extends AbstractState<Double> {
            protected final double[] localCoords;

            public State(@NonNull double[] localCoords, Bounds2d paddedLocalBounds) {
                super(paddedLocalBounds);

                this.localCoords = localCoords;
            }

            @Override
            public Double apply(Void unused) { //stage 2: actually compute the value now that the tiles have been fetched
                return DoubleTiledDataset.this.blend.get(this.localCoords[0], this.localCoords[1], this);
            }
        }

        double[] localCoords = this.projection.fromGeo(lon, lat);

        Bounds2d paddedLocalBounds = Bounds2d.of(localCoords[0], localCoords[0], localCoords[1], localCoords[1])
                .expand(this.blend.size)
                .validate(this.projection, false);

        return new State(localCoords, paddedLocalBounds).future();
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        if (notNegative(sizeX, "sizeX") == 0 | notNegative(sizeZ, "sizeZ") == 0) { //no input points -> no output points, ez
            return CompletableFuture.completedFuture(new double[0]);
        }

        class State extends AbstractState<double[]> {
            protected final CornerBoundingBox2d localBounds;

            public State(@NonNull CornerBoundingBox2d localBounds, Bounds2d paddedLocalBounds) {
                super(paddedLocalBounds);

                this.localBounds = localBounds;
            }

            @Override
            public double[] apply(Void unused) { //stage 2: actually compute the values now that the tiles have been fetched
                BlendMode blend = DoubleTiledDataset.this.blend;

                double stepX = 1.0d / sizeX;
                double stepZ = 1.0d / sizeZ;

                double[] point = new double[2];
                double[] out = new double[sizeX * sizeZ];

                double fx = 0.0d;
                for (int i = 0, x = 0; x < sizeX; x++, fx += stepX) {
                    double fz = 0.0d;
                    for (int z = 0; z < sizeZ; z++, fz += stepZ) {
                        //compute coordinates of point
                        point = this.localBounds.point(point, fx, fz);

                        //sample value at point
                        out[i++] = blend.get(point[0], point[1], this);
                    }
                }

                return out;
            }
        }

        CornerBoundingBox2d localBounds = bounds.fromGeo(this.projection);
        Bounds2d paddedLocalBounds = localBounds.axisAlign().expand(this.blend.size).validate(this.projection, false);

        return new State(localBounds, paddedLocalBounds).future();
    }

    @RequiredArgsConstructor
    protected abstract class AbstractState<R> implements Function<Void, R>, IntToDoubleBiFunction {
        final Long2ObjectMap<double[]> loadedTiles = new Long2ObjectOpenHashMap<>();

        @NonNull
        protected final Bounds2d paddedLocalBounds;

        @Override
        public double apply(int x, int z) { //gets raw sample values to be used in blending
            double[] tile = this.loadedTiles.get(BinMath.packXY(x >> TILE_SHIFT, z >> TILE_SHIFT));
            if (tile == null) {
                return Double.NaN;
            }
            return tile[(z & TILE_MASK) << TILE_SHIFT | (x & TILE_MASK)];
        }

        public CompletableFuture<R> future() {
            ChunkPos[] tilePositions = this.paddedLocalBounds.toTiles(TILE_SIZE);

            return CompletableFuture.allOf(Arrays.stream(tilePositions)
                    .map(pos -> DoubleTiledDataset.this.getAsync(pos)
                            .thenApply(tile -> { //put tile directly into map when it's loaded
                                //synchronize because we can't be certain that all of the futures will be completed by the same thread
                                synchronized (this.loadedTiles) {
                                    this.loadedTiles.put(BinMath.packXY(pos.x, pos.z), tile);
                                }
                                return tile;
                            }))
                    .toArray(CompletableFuture[]::new))
                    .thenApplyAsync(this);
        }
    }
}
