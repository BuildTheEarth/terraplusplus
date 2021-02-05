package io.github.terra121.generator.data;

import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

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
        if (EarthGenerator.isNullIsland(pos.x, pos.z)) { //set mushroom island height
            Arrays.fill(builder.surfaceHeight(), 1);
        }
    }
}
