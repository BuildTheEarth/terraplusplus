package net.buildtheearth.terraminusminus.generator.data;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import net.buildtheearth.terraminusminus.dataset.IElementDataset;
import net.buildtheearth.terraminusminus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.GeneratorDatasets;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.math.ChunkPos;
import net.buildtheearth.terraminusminus.util.CornerBoundingBox2d;
import net.buildtheearth.terraminusminus.util.bvh.BVH;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

/**
 * @author DaPorkchop_
 */
public class OSMBaker implements IEarthDataBaker<BVH<VectorGeometry>[]> {
    @Override
    public CompletableFuture<BVH<VectorGeometry>[]> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return datasets.<IElementDataset<BVH<VectorGeometry>>>getCustom(EarthGeneratorPipelines.KEY_DATASET_OSM_PARSED)
                .getAsync(bounds.expand(16.0d).toCornerBB(datasets.projection(), false).toGeo());
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, BVH<VectorGeometry>[] regions) {
        if (regions == null) { //there's no data in this chunk... we're going to assume it's completely out of bounds
            Arrays.fill(builder.waterDepth(), (byte) (CachedChunkData.WATERDEPTH_TYPE_OCEAN | ~CachedChunkData.WATERDEPTH_TYPE_MASK));
            return;
        }

        int baseX = pos.getMinBlockX();
        int baseZ = pos.getMinBlockZ();
        Bounds2d chunkBounds = Bounds2d.of(baseX, baseX + 16, baseZ, baseZ + 16);

        Set<VectorGeometry> elements = new TreeSet<>();
        for (BVH<VectorGeometry> region : regions) {
            region.forEachIntersecting(chunkBounds, elements::add);
        }
        elements.forEach(element -> element.apply(builder, pos.x, pos.z, chunkBounds));
    }
}
