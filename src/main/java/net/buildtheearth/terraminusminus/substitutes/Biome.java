package net.buildtheearth.terraminusminus.substitutes;

/**
 * A Minecraft biome.
 * Biomes are identified by their ID.
 * <br>
 * Canonical implementations of this interface can be obtained with {@link #byId(Identifier)} and {@link #parse(String)}.
 * <br>
 * Implementations of this class are expected to be immutable and thread safe.
 *
 * @see <a href="https://minecraft.wiki/w/Biome">The Minecraft wiki on biomes</a>
 *
 * @author Smyler
 *
 */
public interface Biome {

    /**
     * @return the identifier of this biome
     */
    Identifier identifier();

    /**
     * Gets a {@link Biome} instance with the given ID.
     * This may return a cached instance or create a new object if necessary.
     *
     * @param id the ID of the Biome.
     *
     * @return a {@link Biome} implementation with the given ID
     */
    static Biome byId(Identifier id) {
        return BiomeImplementation.get(id);
    }

    /**
     * Parses an identifier and returns a corresponding {@link Biome}.
     * This is just a shortcut for <code>Biome.get(Identifier.parse(serializedBiome)</code>.
     *
     * @param serializedBiome a serialized {@link Identifier}
     *
     * @return a biome with the given identifier
     */
    static Biome parse(String serializedBiome) {
        return byId(Identifier.parse(serializedBiome));
    }
    
}
