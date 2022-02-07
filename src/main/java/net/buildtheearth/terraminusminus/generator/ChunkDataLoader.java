package net.buildtheearth.terraminusminus.generator;

import java.util.concurrent.CompletableFuture;

import com.google.common.cache.CacheLoader;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.generator.data.IEarthDataBaker;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;

/**
 * {@link CacheLoader} implementation for earth generators, which asynchronously aggregates information from multiple datasets and stores it
 * in a {@link CachedChunkData} for use by the generator.
 *
 * @author DaPorkchop_
 */
public class ChunkDataLoader extends CacheLoader<ChunkPos, CompletableFuture<CachedChunkData>> {
	protected final GeneratorDatasets datasets;
	protected final IEarthDataBaker<?>[] bakers;

	public ChunkDataLoader(@NonNull EarthGeneratorSettings settings) {
		this.datasets = settings.datasets();
		this.bakers = EarthGeneratorPipelines.dataBakers(settings);
	}

	@Override
	public CompletableFuture<CachedChunkData> load(@NonNull ChunkPos pos) {
		return IEarthAsyncPipelineStep.getFuture(pos, this.datasets, this.bakers, CachedChunkData::builder);
	}

}