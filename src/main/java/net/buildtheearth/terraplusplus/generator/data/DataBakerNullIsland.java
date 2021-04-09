package net.buildtheearth.terraplusplus.generator.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Sets the surface height at null island to 1.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class DataBakerNullIsland implements IEarthDataBaker<Void> {
    @Override
    public CompletableFuture<Void> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return null;
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, Void data) {
        if (EarthGenerator.isNullIsland(pos.x, pos.z)) {
            Arrays.fill(builder.surfaceHeight(), -1);

            byte[] trees = builder.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, null);
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
