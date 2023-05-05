package net.buildtheearth.terraplusplus.generator;

import net.buildtheearth.terraplusplus.TerraMod;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.TilePos;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.buildtheearth.terraplusplus.util.compat.sis.SISHelper;
import net.buildtheearth.terraplusplus.util.geo.pointarray.AxisAlignedGridPointArray2D;
import net.buildtheearth.terraplusplus.util.geo.pointarray.PointArray2D;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
public interface IEarthAsyncPipelineStep<D, V, B extends IEarthAsyncDataBuilder<V>> {
    static <V, B extends IEarthAsyncDataBuilder<V>> CompletableFuture<V> getFuture(TilePos pos, GeneratorDatasets datasets, IEarthAsyncPipelineStep<?, V, B>[] steps, Supplier<B> builderFactory) {
        int baseX = pos.blockX();
        int baseZ = pos.blockZ();
        int sizeBlocks = pos.sizeBlocks();

        CompletableFuture<?>[] futures = new CompletableFuture[steps.length];
        try {
            Bounds2d chunkBounds = Bounds2d.of(baseX, baseX + sizeBlocks, baseZ, baseZ + sizeBlocks);
            CornerBoundingBox2d chunkBoundsGeo = chunkBounds.toCornerBB(datasets.projection(), false).toGeo();

            PointArray2D sampledPoints = new AxisAlignedGridPointArray2D(null, SISHelper.projectedCRS(datasets.projection()), 16, 16,
                    baseX, baseZ, sizeBlocks, sizeBlocks);

            //TODO: don't do this
            sampledPoints = (PointArray2D) sampledPoints.convert(TPP_GEO_CRS, 0.1d);

            for (int i = 0; i < steps.length; i++) {
                try {
                    futures[i] = steps[i].requestData(pos, datasets, chunkBounds, chunkBoundsGeo, sampledPoints);
                } catch (OutOfProjectionBoundsException ignored) {
                }
            }
        } catch (OutOfProjectionBoundsException ignored) {
        }

        boolean areAnyFuturesNull = Arrays.stream(futures).anyMatch(Objects::isNull);
        CompletableFuture<?>[] nonNullFutures = areAnyFuturesNull
                ? Arrays.stream(futures).filter(Objects::nonNull).toArray(CompletableFuture[]::new)
                : futures;

        CompletableFuture<V> future = (nonNullFutures.length != 0 ? CompletableFuture.allOf(nonNullFutures) : CompletableFuture.completedFuture(null))
                .thenApply(unused -> {
                    B builder = builderFactory.get();

                    for (int i = 0; i < steps.length; i++) {
                        CompletableFuture<?> stepFuture = futures[i];
                        steps[i].bake(pos, builder, stepFuture != null ? uncheckedCast(stepFuture.join()) : null);
                    }

                    return builder.build();
                });
        future.whenComplete((data, t) -> {
            if (t != null) {
                TerraMod.LOGGER.error("async exception while loading data", t);
            }
        });
        return future;
    }

    /**
     * Asynchronously fetches the data required to bake the data for the given tile.
     *
     * @param pos           the position of the tile
     * @param datasets      the datasets to be used
     * @param bounds        the bounding box of the chunk (in blocks)
     * @param boundsGeo     the bounding box of the chunk (in world coordinates)
     * @param sampledPoints
     * @return a {@link CompletableFuture} which will be completed with the required data
     */
    CompletableFuture<D> requestData(TilePos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo, PointArray2D sampledPoints) throws OutOfProjectionBoundsException;

    /**
     * Bakes the retrieved data into the chunk data for the given tile.
     *
     * @param pos     the position of the tile
     * @param builder the builder for the cached chunk data
     * @param data    the data to bake
     */
    void bake(TilePos pos, B builder, D data);
}
