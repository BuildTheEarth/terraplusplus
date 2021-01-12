package io.github.terra121.generator.cache;

import com.google.common.cache.CacheLoader;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.TerraMod;
import io.github.terra121.dataset.osm.OSMRegion;
import io.github.terra121.dataset.osm.element.Element;
import io.github.terra121.dataset.osm.poly.OSMPolygon;
import io.github.terra121.dataset.osm.segment.OSMSegment;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.EqualsTieBreakComparator;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;

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

        int[] heightsOut = builder.heights;
        for (int i = 0; i < 16 * 16; i++) { //replace NaNs with blank height value
            heightsOut[i] = Double.isNaN(heights[i]) ? CachedChunkData.BLANK_HEIGHT : floorI(heights[i]);
        }
    }

    protected static void renderPolygon(int baseX, int baseZ, @NonNull int[] wateroffs, @NonNull OSMPolygon polygon) {
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

            CompletableFuture<CachedChunkData> future = CompletableFuture.allOf(heightsFuture, treeCoverFuture, osmRegionsFuture)
                    .thenApply(unused -> {
                        //dereference all futures
                        double[] heights = heightsFuture.join();
                        double treeCover = treeCoverFuture.join();
                        OSMRegion[] osmRegions = osmRegionsFuture.join();

                        CachedChunkData.Builder builder = CachedChunkData.builder()
                                .treeCover(Double.isNaN(treeCover) ? 0.0d : treeCover);

                        applyHeights(builder, heights);

                        //find all segments and polygons that intersect the chunk
                        Set<Element> elements = new TreeSet<>(new EqualsTieBreakComparator<Element>(Comparator.naturalOrder(), true, true));
                        for (OSMRegion region : osmRegions) {
                            region.elements.forEachIntersecting(chunkBounds, elements::add);
                        }

                        elements.forEach(element -> element.apply(builder, pos.x, pos.z, chunkBounds));

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
