package io.github.terra121.generator.cache;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.dataset.osm.poly.OSMPolygon;
import io.github.terra121.dataset.osm.segment.OSMSegment;
import io.github.terra121.generator.EarthGenerator;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

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
        BLANK = new CachedChunkData(defaultHeights, defaultWateroffs, Collections.emptySet(), Collections.emptySet(), 0.0d);

        defaultHeights = new double[16 * 16];
        Arrays.fill(defaultHeights, 1.0d);
        NULL_ISLAND = new CachedChunkData(defaultHeights, defaultWateroffs, Collections.emptySet(), Collections.emptySet(), 0.0d);
    }

    public final double[] heights;
    public final double[] wateroffs;

    private final OSMSegment[] segments;
    private final OSMPolygon[] polygons;

    private final int surfaceMinCube;
    private final int surfaceMaxCube;

    private final double treeCover;

    public CachedChunkData(@NonNull double[] heights, @NonNull double[] wateroffs, Set<OSMSegment> segments, Set<OSMPolygon> polygons, double treeCover) {
        this.heights = heights;
        this.wateroffs = wateroffs;
        this.treeCover = treeCover;

        Arrays.sort(this.segments = segments.toArray(new OSMSegment[0]));
        Arrays.sort(this.polygons = polygons.toArray(new OSMPolygon[0]));

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
