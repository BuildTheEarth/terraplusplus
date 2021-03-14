package net.buildtheearth.terraminusminus.generator.data;

import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.generator.IEarthAsyncPipelineStep;

/**
 * @author DaPorkchop_
 */
public interface IEarthDataBaker<D> extends IEarthAsyncPipelineStep<D, CachedChunkData, CachedChunkData.Builder> {
}
