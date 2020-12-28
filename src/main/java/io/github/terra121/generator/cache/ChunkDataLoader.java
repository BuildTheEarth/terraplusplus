package io.github.terra121.generator.cache;

import com.google.common.cache.CacheLoader;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.dataset.osm.OSMRegion;
import io.github.terra121.dataset.osm.poly.Polygon;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.lang.Math.*;

/**
 * {@link CacheLoader} implementation for {@link EarthGenerator} which asynchronously aggregates information from multiple datasets and stores it
 * in a {@link CachedChunkData} for use by the generator.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class ChunkDataLoader extends CacheLoader<ChunkPos, CompletableFuture<CachedChunkData>> {
    private static double[] fixHeights(double[] heights) {
        if (heights == null) { //consider heights array to be filled with NaNs
            return CachedChunkData.BLANK.heights;
        }

        for (int i = 0; i < 16 * 16; i++) { //replace NaNs with blank height value
            if (Double.isNaN(heights[i])) {
                heights[i] = CachedChunkData.BLANK_HEIGHT;
            }
        }
        return heights;
    }

    protected static void renderPolygon(int baseX, int baseZ, @NonNull double[] wateroffs, @NonNull Polygon polygon) {
        polygon.rasterizeDistance(baseX, 16, baseZ, 16, 5, (x, z, dist) -> {
            int i = (x - baseX) * 16 + (z - baseZ);
            wateroffs[i] = max(wateroffs[i], dist);
        });
    }

    @NonNull
    protected final GeneratorDatasets data;

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

            CornerBoundingBox2d chunkBoundsGeo = chunkBounds.toCornerBB(this.data.projection, false).toGeo();
            CornerBoundingBox2d osmBoundsGeo = osmBounds.toCornerBB(this.data.projection, false).toGeo();

            CompletableFuture<double[]> heightsFuture = this.data.heights.getAsync(chunkBoundsGeo, 16, 16);
            CompletableFuture<Double> treeCoverFuture = this.data.trees.getAsync(chunkBoundsGeo.point(null, 0.5d, 0.5d));
            CompletableFuture<OSMRegion[]> osmRegionsFuture = this.data.osm.getRegionsAsync(osmBoundsGeo);

            return CompletableFuture.allOf(heightsFuture, treeCoverFuture, osmRegionsFuture)
                    .thenApply(unused -> {
                        //dereference all futures
                        double[] heights = fixHeights(heightsFuture.join());
                        double treeCover = treeCoverFuture.join();
                        OSMRegion[] osmRegions = osmRegionsFuture.join();

                        double[] wateroffs = CachedChunkData.NULL_ISLAND.wateroffs.clone();

                        //find all segments and polygons that intersect the chunk
                        Set<Segment> segments = new HashSet<>();
                        for (OSMRegion region : osmRegions) {
                            region.segments.forEachIntersecting(osmBounds, segments::add);

                            //render polygons to create water offsets
                            region.polygons.forEachIntersecting(osmBounds, polygon -> renderPolygon(baseX, baseZ, wateroffs, polygon));
                        }

                        if (Double.isNaN(treeCover)) { //ensure that treeCover is set
                            treeCover = 0.0d;
                        }

                        return new CachedChunkData(heights, wateroffs, segments, treeCover);
                    });
        } catch (OutOfProjectionBoundsException e) {
            return CompletableFuture.completedFuture(CachedChunkData.BLANK);
        }
    }
}
