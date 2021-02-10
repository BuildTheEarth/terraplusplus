package net.buildtheearth.terraplusplus.generator.biome;

import net.buildtheearth.terraplusplus.generator.ChunkBiomesBuilder;
import net.buildtheearth.terraplusplus.generator.IEarthAsyncPipelineStep;
import net.buildtheearth.terraplusplus.util.ImmutableCompactArray;
import net.minecraft.world.biome.Biome;

/**
 * @author DaPorkchop_
 */
public interface IEarthBiomeFilter<D> extends IEarthAsyncPipelineStep<D, ImmutableCompactArray<Biome>, ChunkBiomesBuilder> {
}
