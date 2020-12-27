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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.util.PValidation.*;

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

    protected static void renderPolygon(int baseX, int baseZ, @NonNull double[] wateroffs, @NonNull Polygon polygon) {
        polygon.rasterizeShape(baseX, 16, baseZ, 16, (x, z) -> {
            int i = (x - baseX) * 16 + (z - baseZ);
            checkIndex((i & 0xFF) == i, "base: (%d, %d), xz: (%d, %d)", baseX, baseZ, x, z);
            wateroffs[i] = 1.0d;
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
            //CompletableFuture<double[]> wateroffsFuture = this.data.osm.water.getAsync(chunkBoundsGeo, 16, 16);
            CompletableFuture<Double> treeCoverFuture = this.data.trees.getAsync(chunkBoundsGeo.point(null, 0.5d, 0.5d));
            CompletableFuture<OSMRegion[]> osmRegionsFuture = this.data.osm.getRegionsAsync(osmBoundsGeo);

            return CompletableFuture.allOf(heightsFuture/*, wateroffsFuture*/, treeCoverFuture, osmRegionsFuture)
                    .thenApply(unused -> {
                        //dereference all futures
                        double[] heights = fixHeights(heightsFuture.join());
                        //double[] wateroffs = wateroffsFuture.join();
                        double treeCover = treeCoverFuture.join();
                        OSMRegion[] osmRegions = osmRegionsFuture.join();

                        //find all segments and polygons that intersect the chunk
                        Set<Segment> segments = new HashSet<>();
                        Set<Polygon> polygons = new HashSet<>();
                        for (OSMRegion region : osmRegions) {
                            region.segments.forEachIntersecting(osmBounds, segments::add);
                            region.polygons.forEachIntersecting(osmBounds, polygons::add);
                        }

                        //render polygons to create water offsets
                        double[] wateroffs = new double[16 * 16];
                        for (Polygon polygon : polygons) {
                            renderPolygon(baseX, baseZ, wateroffs, polygon);
                        }

                        combineHeightsWithWateroffs(heights, wateroffs);

                        if (Double.isNaN(treeCover)) { //ensure that treeCover is set
                            treeCover = 0.0d;
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
