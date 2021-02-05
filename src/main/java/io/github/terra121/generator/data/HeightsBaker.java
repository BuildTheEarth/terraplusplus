package io.github.terra121.generator.data;

import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
public class HeightsBaker implements IEarthDataBaker<double[]> {
    @Override
    public CompletableFuture<double[]> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return datasets.heights().getAsync(boundsGeo, 16, 16);
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, double[] heights) {
        if (heights == null) { //consider heights array to be filled with NaNs
            return; //we assume the builder's heights are already all set to the blank height value
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double height = heights[x * 16 + z];
                if (!Double.isNaN(height)) {
                    builder.surfaceHeight(x, z, floorI(height));
                }
            }
        }
    }
}
