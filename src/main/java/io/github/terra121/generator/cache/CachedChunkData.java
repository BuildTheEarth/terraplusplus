package io.github.terra121.generator.cache;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.util.ImmutableBlockStateArray;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.ref.ThreadRef;
import net.minecraft.block.state.IBlockState;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * A collection of data cached per-column by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
@Getter
public class CachedChunkData {
    private static final Ref<Builder> BUILDER_CACHE = ThreadRef.soft(Builder::new);

    public static final int BLANK_HEIGHT = -2;

    public static final CachedChunkData BLANK;
    public static final CachedChunkData NULL_ISLAND;

    static {
        Builder builder = builder();

        BLANK = builder.build();

        Arrays.fill(builder.heights(), 1);
        NULL_ISLAND = builder.build();
    }

    public static Builder builder() {
        return BUILDER_CACHE.get().reset();
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

    public final int[] heights;
    public final int[] wateroffs;

    private final ImmutableBlockStateArray surfaceBlocks;

    private final int surfaceMinCube;
    private final int surfaceMaxCube;

    private final double treeCover;

    private CachedChunkData(@NonNull int[] heights, @NonNull int[] wateroffs, @NonNull ImmutableBlockStateArray surfaceBlocks, double treeCover) {
        this.heights = heights;
        this.wateroffs = wateroffs;
        this.treeCover = treeCover;

        this.surfaceBlocks = surfaceBlocks;

        //this.segments = approximateSort(elements, new EqualsTieBreakComparator<Element.Cube>(Comparator.naturalOrder(), true, true)).toArray(new Element.Cube[0]);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < 16 * 16; i++) {
            min = Math.min(min, heights[i]);
            max = Math.max(max, heights[i]);
        }
        this.surfaceMinCube = Coords.blockToCube(min);
        this.surfaceMaxCube = Coords.blockToCube(max + 1);
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

    public int heightWithWater(int x, int z) {
        int i = x * 16 + z;
        int height = this.heights[i];
        int wateroff = this.wateroffs[i];
        if (wateroff >= EarthGenerator.WATEROFF_TRANSITION) {
            height = height - wateroff + EarthGenerator.WATEROFF_TRANSITION;
        }
        return height;
    }

    /**
     * Builder class for {@link CachedChunkData}.
     *
     * @author DaPorkchop_
     */
    @Getter
    @Setter
    public static final class Builder {
        protected final int[] heights = new int[16 * 16];
        protected final int[] wateroffs = new int[16 * 16];
        protected final IBlockState[] surfaceBlocks = new IBlockState[16 * 16];
        protected double treeCover = 0.0d;

        /**
         * @deprecated use {@link #builder()} unless you have a specific reason to invoke this constructor directly
         */
        @Deprecated
        public Builder() {
            this.reset();
        }

        public Builder reset() {
            Arrays.fill(this.heights, BLANK_HEIGHT);
            Arrays.fill(this.wateroffs, EarthGenerator.WATEROFF_TRANSITION);
            Arrays.fill(this.surfaceBlocks, null);
            this.treeCover = 0.0d;
            return this;
        }

        public CachedChunkData build() {
            return new CachedChunkData(this.heights.clone(), this.wateroffs.clone(), new ImmutableBlockStateArray(this.surfaceBlocks), this.treeCover);
        }
    }
}
