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
        for (org.bukkit.block.Biome bukkitBiome : org.bukkit.block.Biome.values()) {
            Biome terraBiome = Biome.byId(fromBukkitNamespacedKey(bukkitBiome.getKey()));
            assertSame(bukkitBiome, toBukkitBiome(terraBiome));
        }

        // Biomes that Bukkit API doesn't know about
        assertThrows(TranslateToForeignObjectException.class, () -> toBukkitBiome(Biome.parse("invalid:doesnotexist")) );

        assertNull(toBukkitBiome(null));
    }


    @Test
    @DisplayName("Convert Bukkit API biomes to Terra-- biomes")
    void fromBukkitBiomeTest() {
        for (org.bukkit.block.Biome bukkitBiome : org.bukkit.block.Biome.values()) {
            assertEquals(fromBukkitNamespacedKey(bukkitBiome.getKey()), fromBukkitBiome(bukkitBiome).identifier());
        }
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
