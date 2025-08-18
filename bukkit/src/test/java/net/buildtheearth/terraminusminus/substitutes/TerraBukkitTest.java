package net.buildtheearth.terraminusminus.substitutes;

import net.buildtheearth.terraminusminus.substitutes.exceptions.TranslateToForeignObjectException;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.buildtheearth.terraminusminus.substitutes.TerraBukkit.*;
import static org.junit.jupiter.api.Assertions.*;

public class TerraBukkitTest {

    @Test
    void toBukkitNamespacedKeyTest() {
        Identifier id = new Identifier("example", "test");
        NamespacedKey nk = toBukkitNamespacedKey(id);
        assertNamespacedKeyMatchesIdentifier(id, nk);
        assertNull(toBukkitNamespacedKey(null));
    }

    @Test
    void fromBukkitNamespacedKeyTest() {
        NamespacedKey nk = NamespacedKey.fromString("example:test");
        assertNamespacedKeyMatchesIdentifier(fromBukkitNamespacedKey(nk), nk);
        assertNull(fromBukkitNamespacedKey(null));
    }

    @Test
    @DisplayName("Convert Terra-- biomes to Bukkit API biomes")
    void toBukkitBiomeTest() {
        // Biomes that Bukkit knows about
        assertEquals(org.bukkit.block.Biome.BIRCH_FOREST, toBukkitBiome(Biome.parse("birch_forest")));
        assertEquals(org.bukkit.block.Biome.DARK_FOREST, toBukkitBiome(Biome.parse("dark_forest")));

        // Biomes that Bukkit API doesn't know about
        assertThrows(TranslateToForeignObjectException.class, () -> toBukkitBiome(Biome.parse("invalid:doesnotexist")) );

        assertNull(toBukkitBiome(null));
    }


    @Test
    @DisplayName("Convert Bukkit API biomes to Terra-- biomes")
    void fromBukkitBiomeTest() {
        assertEquals(Biome.parse("birch_forest"), fromBukkitBiome(org.bukkit.block.Biome.BIRCH_FOREST));
        assertEquals(Biome.parse("dark_forest"), fromBukkitBiome(org.bukkit.block.Biome.DARK_FOREST));
        assertNull(fromBukkitBiome(null));
    }

    private static void assertNamespacedKeyMatchesIdentifier(Identifier expected, NamespacedKey actual) {
        if (expected == null && actual == null) {
            return;
        }
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.toString(), actual.toString());
    }

}
