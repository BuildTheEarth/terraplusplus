package net.buildtheearth.terraplusplus.generator.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthBiomeProvider;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.ImmutableCompactArray;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class DataBakerInitialBiomes implements IEarthDataBaker<ImmutableCompactArray<Biome>> {
    @Override
    public CompletableFuture<ImmutableCompactArray<Biome>> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return datasets.settings().biomeProvider().getBiomesForChunkAsync(pos);
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, ImmutableCompactArray<Biome> biomes) {
        if (biomes == null) { //can occur if chunk coordinates are outside projection bounds
            return;
        }

        for (int zz = 0; zz < 16; zz++) {
            for (int xx = 0; xx < 16; xx++) { //reverse coordinate order
                builder.biomes()[zz * 16 + xx] = biomes.get(xx * 16 + zz);
            }
        }
    }
}
