package net.buildtheearth.terraplusplus.generator.biome;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.ChunkBiomesBuilder;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Generates a single, fixed biome in the entire world.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class BiomeFilterConstant implements IEarthBiomeFilter<Void> {
    protected final Biome biome;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BiomeFilterConstant(
            @JsonProperty(value = "biome", required = true) @NonNull Biome biome) {
        this.biome = biome;
    }

    @Override
    public CompletableFuture<Void> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return null;
    }

    @Override
    public void bake(ChunkPos pos, ChunkBiomesBuilder builder, Void data) {
        Arrays.fill(builder.state(), this.biome);
    }
}
