package io.github.terra121.dataset.multires;

import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.projection.transform.ScaleProjection;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.IntToDoubleBiFunction;
import io.github.terra121.util.bvh.Bounds2i;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.math.BinMath;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public abstract class DoubleMultiresDataset extends MultiresDataset<double[]> implements ScalarDataset {
    protected static final int TILE_SHIFT = 8;
    protected static final int TILE_SIZE = 1 << TILE_SHIFT; //256
    protected static final int TILE_MASK = (1 << TILE_SHIFT) - 1; //0xFF

    protected final BlendMode blend;

    public DoubleMultiresDataset(GeographicProjection proj, double tileSize, @NonNull BlendMode blend) {
        super(new ScaleProjection(proj, tileSize), 1.0d / tileSize * TILE_SIZE);

        this.blend = blend;
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        if (notNegative(sizeX, "sizeX") == 0 | notNegative(sizeZ, "sizeZ") == 0) { //no input points -> no output points, ez
            return CompletableFuture.completedFuture(new double[0]);
        }

        class State extends AbstractState<double[]> {
            protected final CornerBoundingBox2d localBounds;

            public State(@NonNull CornerBoundingBox2d localBounds, Bounds2i paddedLocalBounds) {
                super(paddedLocalBounds);

                this.localBounds = localBounds;
            }

            @Override
            public double[] apply(Void unused) { //stage 2: actually compute the values now that the tiles have been fetched
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
                        out[i++] = this.sample(point[0], point[1]);
                    }
                }

                return out;
            }
        }

        CornerBoundingBox2d localBounds = bounds.fromGeo(this.projection);
        Bounds2i paddedLocalBounds = localBounds.axisAlign().toInt().expand(this.blend.size).validate(this.projection, false);

        return new State(localBounds, paddedLocalBounds).future;
    }

    @RequiredArgsConstructor
    private static class Layer extends Long2ObjectOpenHashMap<double[]> implements IntToDoubleBiFunction {
        protected final double scale;
        protected final int zoom;

        @Override
        public double apply(int x, int z) {
            double[] tile = this.get(BinMath.packXY(x >> TILE_SHIFT, z >> TILE_SHIFT));
            if (tile == null) {
                throw MissingTileDataException.INSTANCE;
            }
            return tile[(z & TILE_MASK) << TILE_SHIFT | (x & TILE_MASK)];
        }
    }

    protected static final class MissingTileDataException extends RuntimeException {
        public static final MissingTileDataException INSTANCE = new MissingTileDataException();

        private MissingTileDataException() {
            super(null, null, false, false);
        }
    }

    protected abstract class AbstractState<R> implements Function<Void, R> {
        final Layer[] layers;

        final Bounds2i paddedLocalBounds;

        final CompletableFuture<R> future;

        public AbstractState(@NonNull Bounds2i paddedLocalBounds) {
            this.paddedLocalBounds = paddedLocalBounds;

            MultiresConfig config = DoubleMultiresDataset.this.config();

            int[] allZooms = config.getAllIntersecting(paddedLocalBounds)
                    .stream()
                    .mapToInt(WrappedUrl::zoom)
                    .distinct()
                    .sorted()
                    .toArray();

            for (int i = 0, len = allZooms.length; i < len >> 1; i++) { //reverse array so that zooms go from highest to lowest
                int j = len - i - 1;
                int o = allZooms[j];
                allZooms[j] = allZooms[i];
                allZooms[i] = o;
            }

            this.layers = Arrays.stream(allZooms)
                    .mapToObj(zoom -> new Layer(TILE_SIZE / (double) (1 << (config.maxZoom - zoom)), zoom))
                    .toArray(Layer[]::new);

            if (allZooms.length == 0) { //no data...
                this.future = CompletableFuture.completedFuture(null);
                return;
            }

            List<CompletableFuture<double[]>> futures = new ArrayList<>();
            for (int i = 0; i < allZooms.length; i++) {
                int zoom = allZooms[i];
                ChunkPos[] tilePositions = paddedLocalBounds.toTiles(1 << (config.maxZoom - zoom));
                int _i = i; //c'mon java pls
                for (ChunkPos tilePosition : tilePositions) {
                    futures.add(DoubleMultiresDataset.this.getTileAsync(tilePosition.x, zoom, tilePosition.z)
                            .thenApply(tile -> { //put tile directly into map when it's loaded
                                //synchronize because we can't be certain that all of the futures will be completed by the same thread
                                synchronized (this.layers[_i]) {
                                    this.layers[_i].put(BinMath.packXY(tilePosition.x, tilePosition.z), tile);
                                }
                                return tile;
                            }));
                }
            }

            this.future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApplyAsync(this);
        }

        protected double sample(double lon, double lat) {
            for (Layer layer : this.layers) {
                try {
                    return DoubleMultiresDataset.this.blend.get(lon * layer.scale, lat * layer.scale, layer);
                } catch (MissingTileDataException e) { //the layer doesn't have data, proceed to next layer
                }
            }
            throw MissingTileDataException.INSTANCE;
        }
    }
}
