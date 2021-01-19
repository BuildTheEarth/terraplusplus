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
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A collection of data cached per-column by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
public class CachedChunkData {
    public static final int BLANK_HEIGHT = -1;

    public static final int WATERDEPTH_DEFAULT = (byte) 0x80;

    public static final int WATERDEPTH_TYPE_MASK = (byte) 0xC0;
    public static final int WATERDEPTH_TYPE_DEFAULT = (byte) 0x80;
    public static final int WATERDEPTH_TYPE_WATER = (byte) 0x00;
    public static final int WATERDEPTH_TYPE_OCEAN = (byte) 0x40;

    private static final Ref<Builder> BUILDER_CACHE = ThreadRef.soft(Builder::new);

    public static final CachedChunkData BLANK;
    public static final CachedChunkData NULL_ISLAND;

    static {
        Builder builder = builder();

        BLANK = builder.build();

        Arrays.fill(builder.surfaceHeight, 1);
        NULL_ISLAND = builder.build();
    }

    public static Builder builder() {
        return BUILDER_CACHE.get().reset();
    }

    private static int extractActualDepth(int waterDepth) {
        //discard upper 2 bits from least significant byte and then sign-extend everything back down
        return ((waterDepth & 0x3F) - 32) << 26 >> 26;
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

    private final int[] surfaceHeight;
    private final int[] groundHeight;

    private final byte[] biomes;

    @Getter
    private final ImmutableBlockStateArray surfaceBlocks;

    private final int surfaceMinCube;
    private final int surfaceMaxCube;

    @Getter
    private final double treeCover;

    private CachedChunkData(@NonNull Builder builder) {
        this.surfaceHeight = builder.surfaceHeight.clone();
        this.groundHeight = builder.surfaceHeight.clone();

        for (int i = 0; i < 16 * 16; i++) {
            int waterDepth = builder.waterDepth[i];
            int d = extractActualDepth(waterDepth);

            switch (waterDepth & WATERDEPTH_TYPE_MASK) {
                case WATERDEPTH_TYPE_DEFAULT: //no water
                    //do nothing
                    break;
                case WATERDEPTH_TYPE_WATER: //water - lake/river/pond
                    this.groundHeight[i] -= max(d + EarthGenerator.WATER_DEPTH_OFFSET, 0);
                    break;
                case WATERDEPTH_TYPE_OCEAN:
                    if (d < 0) {
                        double t = (~d) / 8.0d;
                        this.surfaceHeight[i] = floorI(lerp(0.0d, this.surfaceHeight[i], t));
                        this.groundHeight[i] = floorI(lerp(-1.0d, this.groundHeight[i], t));
                    } else {
                        this.surfaceHeight[i] = 0;
                        this.groundHeight[i] = min(this.groundHeight[i], -2);
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        this.biomes = new byte[16 * 16];
        for (int i = 0; i < 16 * 16; i++) {
            this.biomes[i] = (byte) Biome.getIdForBiome(builder.biomes[i]);
        }

        this.treeCover = builder.treeCover;

        this.surfaceBlocks = new ImmutableBlockStateArray(builder.surfaceBlocks);

        //this.segments = approximateSort(elements, new EqualsTieBreakComparator<Element.Cube>(Comparator.naturalOrder(), true, true)).toArray(new Element.Cube[0]);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < 16 * 16; i++) {
            min = min(min, this.surfaceHeight[i]);
            max = max(max, this.surfaceHeight[i]);
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

    public int surfaceHeight(int x, int z) {
        return this.surfaceHeight[x * 16 + z];
    }

    public int groundHeight(int x, int z) {
        return this.groundHeight[x * 16 + z];
    }

    public int waterHeight(int x, int z) {
        return this.surfaceHeight(x, z) - 1;
    }

    /**
     * Builder class for {@link CachedChunkData}.
     *
     * @author DaPorkchop_
     */
    @Getter
    @Setter
    public static final class Builder {
        private final int[] surfaceHeight = new int[16 * 16];
        private final byte[] waterDepth = new byte[16 * 16];

        private final Biome[] biomes = new Biome[16 * 16];

        protected final IBlockState[] surfaceBlocks = new IBlockState[16 * 16];
        protected double treeCover = 0.0d;

        /**
         * @deprecated use {@link #builder()} unless you have a specific reason to invoke this constructor directly
         */
        @Deprecated
        public Builder() {
            this.reset();
        }

        public Builder surfaceHeight(int x, int z, int value) {
            this.surfaceHeight[x * 16 + z] = value;
            return this;
        }

        public Builder updateWaterDepth(int x, int z, int depth) {
            depth = ((depth + 32) & 0x3F) | WATERDEPTH_TYPE_WATER;
            if (depth > this.waterDepth[x * 16 + z]) {
                this.waterDepth[x * 16 + z] = (byte) depth;
            }
            return this;
        }

        public Builder updateOceanDepth(int x, int z, int depth) {
            depth = ((depth + 32) & 0x3F) | WATERDEPTH_TYPE_OCEAN;
            if (depth > this.waterDepth[x * 16 + z]) {
                this.waterDepth[x * 16 + z] = (byte) depth;
            }
            return this;
        }

        public int surfaceHeight(int x, int z) {
            return this.surfaceHeight[x * 16 + z];
        }

        public Builder reset() {
            Arrays.fill(this.surfaceHeight, BLANK_HEIGHT);
            Arrays.fill(this.waterDepth, (byte) WATERDEPTH_DEFAULT);
            Arrays.fill(this.surfaceBlocks, null);
            this.treeCover = 0.0d;
            return this;
        }

        public CachedChunkData build() {
            return new CachedChunkData(this);
        }
    }
}
