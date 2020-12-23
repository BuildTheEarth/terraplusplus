package io.github.terra121.generator.cache;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.generator.EarthGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;

/**
 * A collection of data cached per-column by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
@Getter
public class CachedChunkData {
    public static final CachedChunkData BLANK;
    public static final CachedChunkData NULL_ISLAND;

    static {
        double[] defaultWateroffs = new double[16 * 16];

        double[] defaultHeights = new double[16 * 16];
        Arrays.fill(defaultHeights, -2.0d);
        BLANK = new CachedChunkData(defaultHeights, defaultWateroffs);

        defaultHeights = new double[16 * 16];
        Arrays.fill(defaultHeights, 1.0d);
        NULL_ISLAND = new CachedChunkData(defaultHeights, defaultWateroffs);
    }

    public final double[] heights;
    public final double[] wateroffs;
    protected final int surfaceMinCube;
    protected final int surfaceMaxCube;

    public CachedChunkData(@NonNull double[] heights, @NonNull double[] wateroffs) {
        this.heights = heights;
        this.wateroffs = wateroffs;

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 16 * 16; i++) {
            min = Math.min(min, heights[i]);
            max = Math.max(max, heights[i]);
        }
        this.surfaceMinCube = Coords.blockToCube(min);
        this.surfaceMaxCube = Coords.blockToCube(Math.ceil(max));
    }

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
