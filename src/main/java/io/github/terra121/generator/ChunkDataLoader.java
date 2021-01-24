package io.github.terra121.generator;

import com.google.common.cache.CacheLoader;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.TerraMod;
import io.github.terra121.generator.process.IChunkDataBaker;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * {@link CacheLoader} implementation for {@link EarthGenerator} which asynchronously aggregates information from multiple datasets and stores it
 * in a {@link CachedChunkData} for use by the generator.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class ChunkDataLoader extends CacheLoader<ChunkPos, CompletableFuture<CachedChunkData>> {
    private static void applyHeights(@NonNull CachedChunkData.Builder builder, double[] heights) {
        if (heights == null) { //consider heights array to be filled with NaNs
            return; //we assume the builder's heights are already all set to the blank height value
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double height = heights[x * 16 + z];
                if (!Double.isNaN(height)) {
                    builder.surfaceHeight(x, z, floorI(height));
                }
            }
        }
    }

    @NonNull
    protected final GeneratorDatasets data;

    @Override
    public CompletableFuture<CachedChunkData> load(@NonNull ChunkPos pos) {
        if (EarthGenerator.isNullIsland(pos.x, pos.z)) {
            return CompletableFuture.completedFuture(CachedChunkData.NULL_ISLAND);
        }

        try {
            int baseX = Coords.cubeToMinBlock(pos.x);
            int baseZ = Coords.cubeToMinBlock(pos.z);

            Bounds2d chunkBounds = Bounds2d.of(baseX, baseX + 16, baseZ, baseZ + 16);
            CornerBoundingBox2d chunkBoundsGeo = chunkBounds.toCornerBB(this.data.projection(), false).toGeo();

            IChunkDataBaker<?>[] bakers = this.data.bakers;
            CompletableFuture<?>[] futures = new CompletableFuture[bakers.length];
            for (int i = 0; i < bakers.length; i++) {
                futures[i] = bakers[i].requestData(pos, this.data, chunkBounds, chunkBoundsGeo);
            }

            CompletableFuture<CachedChunkData> future = CompletableFuture.allOf(futures)
                    .thenApply(unused -> {
                        CachedChunkData.Builder builder = CachedChunkData.builder();

                        for (int i = 0; i < bakers.length; i++) {
                            bakers[i].bake(pos, builder, uncheckedCast(futures[i].join()));
                        }

                        return builder.build();
                    });
            future.whenComplete((data, t) -> {
                if (t != null) {
                    TerraMod.LOGGER.error("async exception while loading data", t);
                }
            });
            return future;
        } catch (OutOfProjectionBoundsException e) {
            return CompletableFuture.completedFuture(CachedChunkData.BLANK);
        }
    }
}
