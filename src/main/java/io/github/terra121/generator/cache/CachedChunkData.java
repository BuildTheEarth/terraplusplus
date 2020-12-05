package io.github.terra121.generator.cache;

import io.github.terra121.generator.EarthGenerator;
import lombok.Getter;

/**
 * A collection of data cached per-column by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
@Getter
public class CachedChunkData {
    public final double[] heights = new double[16 * 16];
    public final double[] wateroffs = new double[16 * 16];
    protected int surfaceMinCube;
    protected int surfaceMaxCube;

    public boolean intersectsSurface(int cubeY) {
        return cubeY >= this.surfaceMinCube && cubeY <= this.surfaceMaxCube;
    }

    public boolean aboveSurface(int cubeY) {
        return cubeY > this.surfaceMaxCube;
    }

    public boolean belowSurface(int cubeY) {
        return cubeY < this.surfaceMinCube;
    }
}
