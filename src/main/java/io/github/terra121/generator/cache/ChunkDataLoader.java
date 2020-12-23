package io.github.terra121.generator.cache;

import com.google.common.cache.CacheLoader;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class ChunkDataLoader extends CacheLoader<ChunkPos, CompletableFuture<CachedChunkData>> {
    @NonNull
    protected final EarthGenerator generator;

    @Override
    public CompletableFuture<CachedChunkData> load(ChunkPos pos) {
        if (EarthGenerator.isNullIsland(pos.x, pos.z)) {
            return CompletableFuture.completedFuture(CachedChunkData.NULL_ISLAND);
        }

        try {
            CornerBoundingBox2d bounds = new CornerBoundingBox2d(this.generator.projection, pos.x << 4, pos.z << 4, 16, 16);

            CompletableFuture<double[]> heights = this.generator.heights.getAsync(bounds, 16, 16);
            CompletableFuture<double[]> waterOffs = this.generator.osm.water.getAsync(bounds, 16, 16);

            return CompletableFuture.allOf(heights, waterOffs)
                    .thenApply(unused -> new CachedChunkData(heights.join(), waterOffs.join()));
        } catch (OutOfProjectionBoundsException e) {
            CompletableFuture<CachedChunkData> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}
