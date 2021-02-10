package io.github.terra121.generator.data;

import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static io.github.terra121.generator.EarthGeneratorPipelines.*;

/**
 * Sets the surface height at null island to 1.
 *
 * @author DaPorkchop_
 */
public class NullIslandBaker implements IEarthDataBaker<Void> {
    @Override
    public CompletableFuture<Void> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return null;
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, Void data) {
        if (EarthGenerator.isNullIsland(pos.x, pos.z)) {
            Arrays.fill(builder.surfaceHeight(), -1);

            byte[] trees = builder.getCustom(KEY_DATA_TREE_COVER, null);
            if (trees != null) {
                Arrays.fill(trees, (byte) 0);
            }

            if (((pos.x ^ (pos.x >> 31)) | (pos.z ^ (pos.z >> 31))) == 0) {
                Arrays.fill(builder.biomes(), Biomes.FOREST);
            } else {
                Arrays.fill(builder.biomes(), Biomes.PLAINS);
            }

            Arrays.fill(builder.waterDepth(), (byte) CachedChunkData.WATERDEPTH_DEFAULT);
        }
    }
}
