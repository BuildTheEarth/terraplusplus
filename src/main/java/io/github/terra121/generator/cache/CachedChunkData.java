package io.github.terra121.generator.cache;

import io.github.terra121.generator.EarthGenerator;

/**
 * A collection of data cached per-column by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
public class CachedChunkData {
    public final double[] heights = new double[16 * 16];
    public final double[] wateroffs = new double[16 * 16];
    public int surfaceMinCube;
    public int surfaceMaxCube;
}
