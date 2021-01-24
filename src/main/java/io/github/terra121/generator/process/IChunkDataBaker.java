package io.github.terra121.generator.process;

import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
public interface IChunkDataBaker<D> {
    /**
     * Asynchronously fetches the data required to bake the data for the given column.
     *
     * @param pos      the position of the column
     * @param datasets the datasets to be used
     * @param bounds
     * @param boundsGeo
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
    void bake(ChunkPos pos, CachedChunkData.Builder builder, D data);
}
