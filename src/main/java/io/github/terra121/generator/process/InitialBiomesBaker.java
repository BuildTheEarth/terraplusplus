package io.github.terra121.generator.process;

import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.GeneratorDatasets;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.BiomeProvider;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class InitialBiomesBaker implements IChunkDataBaker<Void> {
    @NonNull
    protected final BiomeProvider biomeProvider;

    @Override
    public CompletableFuture<Void> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, Void unused) {
        this.biomeProvider.getBiomes(builder.biomes(), pos.getXStart(), pos.getZStart(), 16, 16);
    }
}
