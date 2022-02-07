package net.buildtheearth.terraminusminus.generator.data;

import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.generator.EarthBiomeProvider;
import net.buildtheearth.terraminusminus.generator.GeneratorDatasets;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.util.CornerBoundingBox2d;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class InitialBiomesBaker implements IEarthDataBaker<Void> {
    @NonNull
    protected final EarthBiomeProvider biomeProvider;

    @Override
    public CompletableFuture<Void> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, Void unused) {
        this.biomeProvider.getBiomes(builder.biomes(), pos.getMinBlockX(), pos.getMinBlockZ(), 16, 16);
    }
}
