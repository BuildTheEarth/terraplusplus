package io.github.terra121.generator;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.TerraMod;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
public interface IEarthAsyncPipelineStep<D, V, B extends IEarthAsyncDataBuilder<V>> {
    static <V, B extends IEarthAsyncDataBuilder<V>> CompletableFuture<V> getFuture(ChunkPos pos, GeneratorDatasets datasets, IEarthAsyncPipelineStep<?, V, B>[] steps, Supplier<B> builderFactory) throws OutOfProjectionBoundsException {
        int baseX = Coords.cubeToMinBlock(pos.x);
        int baseZ = Coords.cubeToMinBlock(pos.z);

        Bounds2d chunkBounds = Bounds2d.of(baseX, baseX + 16, baseZ, baseZ + 16);
        CornerBoundingBox2d chunkBoundsGeo = chunkBounds.toCornerBB(datasets.projection(), false).toGeo();

        CompletableFuture<?>[] futures = new CompletableFuture[steps.length];
        for (int i = 0; i < steps.length; i++) {
            futures[i] = steps[i].requestData(pos, datasets, chunkBounds, chunkBoundsGeo);
        }

        boolean areAnyFuturesNull = Arrays.stream(futures).anyMatch(Objects::isNull);
        CompletableFuture<?>[] nonNullFutures = areAnyFuturesNull
                ? Arrays.stream(futures).filter(Objects::nonNull).toArray(CompletableFuture[]::new)
                : futures;

        CompletableFuture<V> future = CompletableFuture.allOf(nonNullFutures)
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
