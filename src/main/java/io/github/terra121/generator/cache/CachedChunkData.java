package io.github.terra121.generator.cache;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.dataset.osm.poly.OSMPolygon;
import io.github.terra121.dataset.osm.segment.OSMSegment;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.util.EqualsTieBreakComparator;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

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

    /**
     * Sorts elements using a {@link TreeSet} rather than using {@link Arrays#sort(Object[])}.
     * <p>
     * This allows for an approximate, but potentially imperfect sort in cases where the comparator isn't transient. {@link Arrays#sort(Object[])} would sooner
     * throw an exception than return imperfect results.
     *
     * @param values     the values to sort
     * @param comparator the comparator to compare values with
     * @param <T>        the value type
     * @return a sorted collection
     */
    private static <T> Collection<T> approximateSort(@NonNull Collection<T> values, @NonNull Comparator<T> comparator) {
        Collection<T> out = new TreeSet<>(comparator);
        out.addAll(values);
        return out;
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

        this.segments = approximateSort(segments, new EqualsTieBreakComparator<OSMSegment>(Comparator.naturalOrder(), true, true)).toArray(new OSMSegment[0]);
        this.polygons = approximateSort(polygons, new EqualsTieBreakComparator<OSMPolygon>(Comparator.naturalOrder(), true, true)).toArray(new OSMPolygon[0]);

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
