package net.buildtheearth.terraminusminus.generator.data;

import static net.daporkchop.lib.common.math.PMath.floorI;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import net.buildtheearth.terraminusminus.dataset.IScalarDataset;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.GeneratorDatasets;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.math.ChunkPos;
import net.buildtheearth.terraminusminus.util.CornerBoundingBox2d;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

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
