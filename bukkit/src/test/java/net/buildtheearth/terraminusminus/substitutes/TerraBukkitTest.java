package net.buildtheearth.terraminusminus.substitutes;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.Test;

import static net.buildtheearth.terraminusminus.substitutes.TerraBukkit.*;
import static org.junit.jupiter.api.Assertions.*;

public class TerraBukkitTest {

    @Test
    void toBukkitNamespacedKeyTest() {
        Identifier id = new Identifier("example", "test");
        NamespacedKey nk = toBukkitNamespacedKey(id);
        assertNamespacedKeyMatchesIdentifier(id, nk);
    }

    @Test
    void fromBukkitNamespacedKeyTest() {
        NamespacedKey nk = NamespacedKey.fromString("example:test");
        assertNamespacedKeyMatchesIdentifier(fromBukkitNamespacedKey(nk), nk);
    }

    private void assertNamespacedKeyMatchesIdentifier(Identifier expected, NamespacedKey actual) {
        if (expected == null && actual == null) {
            return;
        }
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.toString(), actual.toString());
    }

}
