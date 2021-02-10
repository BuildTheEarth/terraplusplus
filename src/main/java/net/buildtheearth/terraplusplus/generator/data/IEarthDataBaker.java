package net.buildtheearth.terraplusplus.generator.data;

import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.IEarthAsyncPipelineStep;

/**
 * @author DaPorkchop_
 */
public interface IEarthDataBaker<D> extends IEarthAsyncPipelineStep<D, CachedChunkData, CachedChunkData.Builder> {
}
