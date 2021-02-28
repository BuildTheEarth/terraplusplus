package net.buildtheearth.terraplusplus.generator.data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.BiomeProvider;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class InitialBiomesBaker implements IEarthDataBaker<Void> {
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
