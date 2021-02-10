package net.buildtheearth.terraplusplus.generator.data;

import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
public class HeightsBaker implements IEarthDataBaker<double[]> {
    @Override
    public CompletableFuture<double[]> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_HEIGHTS).getAsync(boundsGeo, 16, 16);
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, double[] heights) {
        if (heights == null) { //consider heights array to be filled with NaNs
            Arrays.fill(builder.waterDepth(), (byte) (CachedChunkData.WATERDEPTH_TYPE_OCEAN | ~CachedChunkData.WATERDEPTH_TYPE_MASK));
            return; //we assume the builder's heights are already all set to the blank height value
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double height = heights[x * 16 + z];
                if (!Double.isNaN(height)) {
                    builder.surfaceHeight(x, z, floorI(height));
                } else {
                    builder.updateOceanDepth(x, z, 0);
                }
            }
        }
    }
}
