package net.buildtheearth.terraminusminus.substitutes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IdentifierTest {

    @Test
    void canParseIdentifier() {
        Identifier identifier = Identifier.parse("bar:code");
        assertIdentifier("bar", "code", identifier);

        identifier = Identifier.parse("minecraft:zombie");
        assertIdentifier("minecraft", "zombie", identifier);

        identifier = Identifier.parse("diamond");
        assertIdentifier("minecraft", "diamond", identifier);

        identifier = Identifier.parse("minecraft/villager");
        assertIdentifier("minecraft", "minecraft/villager", identifier);

        identifier = Identifier.parse("minecraft:textures/villager.png");
        assertIdentifier("minecraft", "textures/villager.png", identifier);

        assertThrows(IllegalArgumentException.class, () -> Identifier.parse("minecraft:"));
        assertThrows(IllegalArgumentException.class, () -> Identifier.parse(":dirt"));
        assertThrows(IllegalArgumentException.class, () -> Identifier.parse(":"));
        assertThrows(IllegalArgumentException.class, () -> Identifier.parse(""));
        assertThrows(IllegalArgumentException.class, () -> Identifier.parse("foo/bar:coal"));
        assertThrows(IllegalArgumentException.class, () -> Identifier.parse("mymap:schrödingers_var"));
        assertThrows(IllegalArgumentException.class, () -> Identifier.parse("custom_pack:Capital"));
        assertThrows(IllegalArgumentException.class, () -> Identifier.parse("cusTom_pack:capital"));
    }

    @Test
    void canCreateIdentifierWithNamespace() {
        Identifier identifier = Identifier.parse("minecraft:zombie");
        identifier = identifier.withNamespace("mymod");
        assertIdentifier("mymod", "zombie", identifier);
    }

    @Test
    void canCreateIdentifierWithPath() {
        Identifier identifier = Identifier.parse("minecraft:zombie");
        identifier = identifier.withPath("skeleton");
        assertIdentifier("minecraft", "skeleton", identifier);
    }

    public static void assertIdentifier(String namespace, String path, Identifier identifier) {
        assertEquals(namespace, identifier.namespace());
        assertEquals(path, identifier.path());
    }

}
