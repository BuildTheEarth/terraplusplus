package io.github.terra121.generator.data;

import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.generator.IEarthAsyncPipelineStep;

/**
 * @author DaPorkchop_
 */
public interface IEarthDataBaker<D> extends IEarthAsyncPipelineStep<D, CachedChunkData, CachedChunkData.Builder> {
}
