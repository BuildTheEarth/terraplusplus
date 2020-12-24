package io.github.terra121.generator.cache;

import com.google.common.cache.CacheLoader;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.dataset.osm.OSMRegion;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * {@link CacheLoader} implementation for {@link EarthGenerator} which asynchronously aggregates information from multiple datasets and stores it
 * in a {@link CachedChunkData} for use by the generator.
 *
 * @author DaPorkchop_
 */
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
            int baseX = Coords.cubeToMinBlock(pos.x);
            int baseZ = Coords.cubeToMinBlock(pos.z);

            Bounds2d chunkBounds = Bounds2d.of(baseX, baseX + 16, baseZ, baseZ + 16);
            Bounds2d osmBounds = chunkBounds.expand(8.0d);

            CornerBoundingBox2d chunkBoundsGeo = chunkBounds.toCornerBB(this.generator.projection, false).toGeo();
            CornerBoundingBox2d osmBoundsGeo = osmBounds.toCornerBB(this.generator.projection, false).toGeo();

            CompletableFuture<double[]> heights = this.generator.heights.getAsync(chunkBoundsGeo, 16, 16);
            CompletableFuture<double[]> waterOffs = this.generator.osm.water.getAsync(chunkBoundsGeo, 16, 16);

            CompletableFuture<OSMRegion[]> osmRegions = this.generator.osm.getRegionsAsync(osmBoundsGeo);

            return CompletableFuture.allOf(heights, waterOffs, osmRegions)
                    .thenApply(unused -> {
                        Set<Segment> segments = new HashSet<>();
                        for (OSMRegion region : osmRegions.join()) {
                            region.segments.forEachIntersecting(osmBounds, segments::add);
                        }
                        return new CachedChunkData(heights.join(), waterOffs.join(), segments, Collections.emptySet());
                    });
        } catch (OutOfProjectionBoundsException e) {
            CompletableFuture<CachedChunkData> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}
