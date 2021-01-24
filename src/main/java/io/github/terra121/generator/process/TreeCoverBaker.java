package io.github.terra121.generator.process;

import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.CompletableFuture;

import static io.github.terra121.TerraConstants.*;
import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
public class TreeCoverBaker implements IChunkDataBaker<Double> {
    @Override
    public CompletableFuture<Double> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return datasets.trees().getAsync(boundsGeo.point(null, 0.5d, 0.5d));
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, Double treeCover) {
        builder.putCustom(KEY_TREE_COVER, Double.isNaN(treeCover) ? 0.0d : treeCover);
    }
}
