package io.github.terra121.generator.biome;

import io.github.terra121.generator.ChunkBiomesBuilder;
import io.github.terra121.generator.IEarthAsyncPipelineStep;
import io.github.terra121.util.ImmutableCompactArray;
import net.minecraft.world.biome.Biome;

/**
 * @author DaPorkchop_
 */
public interface IEarthBiomeFilter<D> extends IEarthAsyncPipelineStep<D, ImmutableCompactArray<Biome>, ChunkBiomesBuilder> {
}
