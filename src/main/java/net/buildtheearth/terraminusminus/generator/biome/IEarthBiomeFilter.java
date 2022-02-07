package net.buildtheearth.terraminusminus.generator.biome;

import net.buildtheearth.terraminusminus.generator.ChunkBiomesBuilder;
import net.buildtheearth.terraminusminus.generator.IEarthAsyncPipelineStep;
import net.buildtheearth.terraminusminus.substitutes.Biome;
import net.buildtheearth.terraminusminus.util.ImmutableCompactArray;

/**
 * @author DaPorkchop_
 */
public interface IEarthBiomeFilter<D> extends IEarthAsyncPipelineStep<D, ImmutableCompactArray<Biome>, ChunkBiomesBuilder> {
}
