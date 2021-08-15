package net.buildtheearth.terraplusplus.generator;

import lombok.Getter;
import net.buildtheearth.terraplusplus.util.ImmutableCompactArray;
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.ref.ThreadRef;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;

/**
 * Builds a 16x16 area of chunks.
 *
 * @author DaPorkchop_
 */
@Getter
public class ChunkBiomesBuilder implements IEarthAsyncDataBuilder<ImmutableCompactArray<Biome>> {
    private static final Ref<ChunkBiomesBuilder> BUILDER_CACHE = ThreadRef.soft(ChunkBiomesBuilder::new);

    public static ChunkBiomesBuilder get() {
        return BUILDER_CACHE.get().reset();
    }

    protected final Biome[] state = new Biome[16 * 16];

    public Biome get(int x, int z) {
        return this.state[x * 16 + z];
    }

    public ChunkBiomesBuilder set(int x, int z, Biome biome) {
        this.state[x * 16 + z] = biome;
        return this;
    }

    /**
     * Resets this builder instance so that it may be used again.
     */
    public ChunkBiomesBuilder reset() {
        Arrays.fill(this.state, null);
        return this;
    }

    /**
     * @return the array of biomes in this chunk
     */
    @Override
	public ImmutableCompactArray<Biome> build() {
        for (int i = 0; i < 16 * 16; i++) {
            if (this.state[i] == null) {
                throw new IllegalStateException("all biomes must be set!");
            }
        }
        return new ImmutableCompactArray<>(this.state);
    }
}
