package net.buildtheearth.terraminusminus.substitutes;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
final class BiomeImplementation implements Biome {

    @Getter
    private final @NonNull Identifier identifier;

    private static final LoadingCache<@NotNull Identifier, @NotNull Biome> cache = CacheBuilder.newBuilder()
            .initialCapacity(100)  // Minecraft 1.21.4 has 65 biomes
            .maximumSize(4096)     // This should be high enough to accommodate almost any realistic scenario while remaining a good safeguard
            .build(CacheLoader.from(BiomeImplementation::new));

    static Biome get(Identifier identifier) {
        return BiomeImplementation.cache.getUnchecked(identifier);
    }

    @Override
    public String toString() {
        return this.identifier.toString();
    }

}
