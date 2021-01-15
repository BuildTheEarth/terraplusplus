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

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A collection of data cached per-column by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
public class CachedChunkData {
    private static final Ref<Builder> BUILDER_CACHE = ThreadRef.soft(Builder::new);

    public static final int EXTRA_WATERDEPTH = 0;
    public static final int EXTRA_OCEANDIST = 1;

    public static final int EXTRA_UNSET = 0xFFFFFF80; //unset value for extra data
    public static final int EXTRA_UNSET_UNEXTENDED = EXTRA_UNSET & 0xFF; //unset value, but without sign extension
    public static final int EXTRA_UNSET_WORD = (EXTRA_UNSET_UNEXTENDED << 24) | (EXTRA_UNSET_UNEXTENDED << 16) | (EXTRA_UNSET_UNEXTENDED << 8) | EXTRA_UNSET_UNEXTENDED;

    public static final int BLANK_HEIGHT = -2;

    public static final CachedChunkData BLANK;
    public static final CachedChunkData NULL_ISLAND;

    static {
        Builder builder = builder();

        BLANK = builder.build();

        Arrays.fill(builder.topHeight, (short) 1);
        Arrays.fill(builder.groundHeight, (short) 1);
        NULL_ISLAND = builder.build();
    }

    public static Builder builder() {
        return BUILDER_CACHE.get().reset();
    }

    /**
     * Sorts elements using a {@link TreeSet} rather than using {@link Arrays#sort(Object[])}.
     * <p>
     * This allows for a decent, but potentially imperfect sort in cases where the comparator isn't perfectly consistent. {@link Arrays#sort(Object[])} would sooner
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

    private final short[] topHeight;
    private final short[] groundHeight;
    private final short[] waterHeight;

    @Getter
    private final ImmutableBlockStateArray surfaceBlocks;

    private final int surfaceMinCube;
    private final int surfaceMaxCube;

    @Getter
    private final double treeCover;

    private CachedChunkData(@NonNull Builder builder) {
        this.topHeight = builder.topHeight.clone();
        this.groundHeight = builder.groundHeight.clone();
        this.waterHeight = builder.waterHeight.clone();
        this.treeCover = builder.treeCover;

        this.surfaceBlocks = new ImmutableBlockStateArray(builder.surfaceBlocks);

        //this.segments = approximateSort(elements, new EqualsTieBreakComparator<Element.Cube>(Comparator.naturalOrder(), true, true)).toArray(new Element.Cube[0]);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < 16 * 16; i++) {
            min = min(min, this.topHeight[i]);
            max = max(max, this.topHeight[i]);
        }
        for (int i = 0; i < 16 * 16; i++) {
            min = min(min, this.groundHeight[i]);
            max = max(max, this.groundHeight[i]);
        }
        for (int i = 0; i < 16 * 16; i++) {
            min = min(min, this.waterHeight[i]);
            max = max(max, this.waterHeight[i]);
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

    public int topHeight(int x, int z) {
        return this.topHeight[x * 16 + z];
    }

    public int groundHeight(int x, int z) {
        return this.groundHeight[x * 16 + z];
    }

    public int waterHeight(int x, int z) {
        return this.waterHeight[x * 16 + z];
    }

    /**
     * Builder class for {@link CachedChunkData}.
     *
     * @author DaPorkchop_
     */
    @Getter
    @Setter
    public static final class Builder {
        private final short[] topHeight = new short[16 * 16];
        private final short[] groundHeight = new short[16 * 16];
        private final short[] waterHeight = new short[16 * 16];
        protected final IBlockState[] surfaceBlocks = new IBlockState[16 * 16];
        protected double treeCover = 0.0d;

        /**
         * @deprecated use {@link #builder()} unless you have a specific reason to invoke this constructor directly
         */
        @Deprecated
        public Builder() {
            this.reset();
        }

        public Builder topHeight(int x, int z, int value) {
            this.topHeight[x * 16 + z] = toShort(value);
            return this;
        }

        public Builder groundHeight(int x, int z, int value) {
            this.groundHeight[x * 16 + z] = toShort(value);
            return this;
        }

        public Builder waterHeight(int x, int z, int value) {
            this.waterHeight[x * 16 + z] = toShort(value);
            return this;
        }

        public int topHeight(int x, int z) {
            return this.topHeight[x * 16 + z];
        }

        public int groundHeight(int x, int z) {
            return this.groundHeight[x * 16 + z];
        }

        public int waterHeight(int x, int z) {
            return this.waterHeight[x * 16 + z];
        }

        public Builder copyTopHeightToGroundAndWater() {
            System.arraycopy(this.topHeight, 0, this.groundHeight, 0, 16 * 16);
            for (int i = 0; i < 16 * 16; i++) {
                this.waterHeight[i] = toShort(this.topHeight[i] - 1);
            }
            return this;
        }

        public Builder reset() {
            Arrays.fill(this.topHeight, (short) BLANK_HEIGHT);
            Arrays.fill(this.groundHeight, (short) BLANK_HEIGHT);
            Arrays.fill(this.waterHeight, (short) BLANK_HEIGHT);
            Arrays.fill(this.surfaceBlocks, null);
            this.treeCover = 0.0d;
            return this;
        }

        public CachedChunkData build() {
            return new CachedChunkData(this);
        }
    }
}
