package io.github.terra121.generator.cache;

import com.google.common.cache.CacheLoader;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.dataset.osm.OSMRegion;
import io.github.terra121.dataset.osm.poly.Polygon;
import io.github.terra121.dataset.osm.segment.Segment;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
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
    private static final double[] NAN_ARRAY = new double[16 * 16];

    static {
        Arrays.fill(NAN_ARRAY, Double.NaN);
    }

    private static void combineHeightsWithWateroffs(@NonNull double[] heights, @NonNull double[] wateroffs) {
        //original implementation:
        /*double height = out[z * TILE_SIZE + x];
                    if (height > -1.0d && height != 0.0d && height < 200.0d) {
                        double[] proj;
                        try {
                            proj = this.projection.toGeo(tileX * TILE_SIZE + x, tileZ * TILE_SIZE + z);
                        } catch (OutOfProjectionBoundsException e) { //out of bounds... this is PROBABLY impossible, but you can never be too sure
                            //just leave height as it is in the dataset and proceed to the next sample
                            continue;
                        }
                        double lon = proj[0];
                        double lat = proj[1];

                        double mine = this.water.get(lon, lat);

                        double oceanRadius = 2.0d / (60.0d * 60.0d);
                        if (mine > 1.4d || (height > 10.0d & (mine > 1.0d
                                                              || this.water.get(lon + oceanRadius, lat) > 1.0d
                                                              || this.water.get(lon - oceanRadius, lat) > 1.0d
                                                              || this.water.get(lon, lat + oceanRadius) > 1.0d
                                                              || this.water.get(lon, lat - oceanRadius) > 1.0d))) {
                            height = -1.0d;
                        }
                    }
                    out[z * TILE_SIZE + x] = height;*/

        for (int i = 0; i < 16 * 16; i++) {
            double height = heights[i];
            double wateroff = wateroffs[i];

            if (Double.isNaN(height)) {
                heights[i] = -100.0d;
                wateroffs[i] = 2.0d;
            } else if (Double.isNaN(wateroff)) {
                wateroffs[i] = 0.0d;
            } else if (height > -1.0d && height != 0.0d && height < 200.0d && wateroff > 1.4d) {
                heights[i] = -1.0d;
            }
        }
    }

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

            CompletableFuture<double[]> heightsFuture = this.generator.heights.getAsync(chunkBoundsGeo, 16, 16);
            CompletableFuture<double[]> wateroffsFuture = this.generator.osm.water.getAsync(chunkBoundsGeo, 16, 16);
            CompletableFuture<Double> treeCoverFuture = this.generator.trees.getAsync(chunkBoundsGeo.point(null, 0.5d, 0.5d));
            CompletableFuture<OSMRegion[]> osmRegionsFuture = this.generator.osm.getRegionsAsync(osmBoundsGeo);

            return CompletableFuture.allOf(heightsFuture, wateroffsFuture, treeCoverFuture, osmRegionsFuture)
                    .thenApply(unused -> {
                        //dereference all futures
                        double[] heights = heightsFuture.join();
                        double[] wateroffs = wateroffsFuture.join();
                        double treeCover = treeCoverFuture.join();
                        OSMRegion[] osmRegions = osmRegionsFuture.join();

                        if (heights == null) { //ensure that both arrays are set
                            heights = NAN_ARRAY.clone();
                        }
                        if (wateroffs == null) {
                            wateroffs = NAN_ARRAY.clone();
                        }
                        combineHeightsWithWateroffs(heights, wateroffs);

                        if (Double.isNaN(treeCover)) {
                            treeCover = 0.0d;
                        }

                        //find all segments and polygons that intersect the chunk
                        Set<Segment> segments = new HashSet<>();
                        Set<Polygon> polygons = Collections.emptySet();
                        for (OSMRegion region : osmRegions) {
                            region.segments.forEachIntersecting(osmBounds, segments::add);
                        }

                        return new CachedChunkData(heights, wateroffs, segments, polygons, treeCover);
                    });
        } catch (OutOfProjectionBoundsException e) {
            CompletableFuture<CachedChunkData> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}
