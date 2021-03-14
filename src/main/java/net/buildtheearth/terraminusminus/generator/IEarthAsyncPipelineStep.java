package net.buildtheearth.terraminusminus.generator;

import static net.daporkchop.lib.common.util.PorkUtil.uncheckedCast;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.buildtheearth.terraminusminus.TerraMinusMinus;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.math.ChunkPos;
import net.buildtheearth.terraminusminus.util.CornerBoundingBox2d;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

/**
 * @author DaPorkchop_
 */
public interface IEarthAsyncPipelineStep<D, V, B extends IEarthAsyncDataBuilder<V>> {
    static <V, B extends IEarthAsyncDataBuilder<V>> CompletableFuture<V> getFuture(ChunkPos pos, GeneratorDatasets datasets, IEarthAsyncPipelineStep<?, V, B>[] steps, Supplier<B> builderFactory) {
        int baseX = ChunkPos.cubeToMinBlock(pos.x);
        int baseZ = ChunkPos.cubeToMinBlock(pos.z);

        CompletableFuture<?>[] futures = new CompletableFuture[steps.length];
        try {
            Bounds2d chunkBounds = Bounds2d.of(baseX, baseX + 16, baseZ, baseZ + 16);
            CornerBoundingBox2d chunkBoundsGeo = chunkBounds.toCornerBB(datasets.projection(), false).toGeo();

            for (int i = 0; i < steps.length; i++) {
                try {
                    futures[i] = steps[i].requestData(pos, datasets, chunkBounds, chunkBoundsGeo);
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
                TerraMinusMinus.LOGGER.error("async exception while loading data", t);
            }
        });
        return future;
    }

    /**
     * Asynchronously fetches the data required to bake the data for the given column.
     *
     * @param pos       the position of the column
     * @param datasets  the datasets to be used
     * @param bounds    the bounding box of the chunk (in blocks)
     * @param boundsGeo the bounding box of the chunk (in world coordinates)
     * @return a {@link CompletableFuture} which will be completed with the required data
     */
    CompletableFuture<D> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException;

    /**
     * Bakes the retrieved data into the chunk data for the given column.
     *
     * @param pos     the position of the column
     * @param builder the builder for the cached chunk data
     * @param data    the data to bake
     */
    void bake(ChunkPos pos, B builder, D data);
}
