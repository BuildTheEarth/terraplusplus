package io.github.terra121.generator.cache;

import com.google.common.cache.CacheLoader;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.TerraMod;
import io.github.terra121.dataset.osm.OSMRegion;
import io.github.terra121.dataset.osm.element.Element;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

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

        for (int  x = 0; x < 16; x++) {
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

                        //find all OpenStreetMap geometry that intersects the chunk
                        Set<Element> elements = new TreeSet<>();
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
