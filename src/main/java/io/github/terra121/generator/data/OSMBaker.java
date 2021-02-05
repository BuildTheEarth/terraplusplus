package io.github.terra121.generator.data;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.dataset.osm.OSMRegion;
import io.github.terra121.dataset.osm.OpenStreetMap;
import io.github.terra121.dataset.osm.element.Element;
import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import static io.github.terra121.generator.EarthGeneratorPipelines.*;

/**
 * @author DaPorkchop_
 */
public class OSMBaker implements IEarthDataBaker<OSMRegion[]> {
    @Override
    public CompletableFuture<OSMRegion[]> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return datasets.<OpenStreetMap>getCustom(KEY_DATASET_OSM).getRegionsAsync(bounds.expand(16.0d).toCornerBB(datasets.projection(), false).toGeo());
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, OSMRegion[] regions) {
        if (regions == null) { //there's no data in this chunk... we're going to assume it's completely out of bounds
            Arrays.fill(builder.waterDepth(), (byte) (CachedChunkData.WATERDEPTH_TYPE_OCEAN | ~CachedChunkData.WATERDEPTH_TYPE_MASK));
            return;
        }

        int baseX = Coords.cubeToMinBlock(pos.x);
        int baseZ = Coords.cubeToMinBlock(pos.z);
        Bounds2d chunkBounds = Bounds2d.of(baseX, baseX + 16, baseZ, baseZ + 16);

        Set<Element> elements = new TreeSet<>();
        for (OSMRegion region : regions) {
            region.elements.forEachIntersecting(chunkBounds, elements::add);
        }
        elements.forEach(element -> element.apply(builder, pos.x, pos.z, chunkBounds));
    }
}
