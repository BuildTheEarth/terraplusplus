package net.buildtheearth.terraminusminus.substitutes;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BlockStateTest {

    @Test
    void testBuilder() {
        Identifier dirt = new Identifier("dirt");
        BlockStateBuilder builder = BlockStateBuilder.get();
        assertThrows(IllegalStateException.class, builder::build);

        BlockState state = builder.setBlock(dirt).build();
        assertEquals(new Identifier("minecraft", "dirt"), state.getBlock());
        assertEquals(0, state.getProperties().size());

        state = builder.setBlock(dirt).setProperty("facing", "north").setProperty("facing", "south").build();
        assertEquals(new Identifier("minecraft", "dirt"), state.getBlock());
        assertEquals(1, state.getProperties().size());
        assertEquals("south", state.getProperty("facing").getAsString());

        state = builder.setBlock(dirt).setProperty("dirty", true).build();
        assertEquals(new Identifier("minecraft", "dirt"), state.getBlock());
        assertEquals(1, state.getProperties().size());
        assertTrue(state.getProperty("dirty").canBeBoolean());
        assertTrue(state.getProperty("dirty").getAsBoolean());

        state = builder.setBlock(dirt).setProperty("power", 10).build();
        assertEquals(new Identifier("minecraft", "dirt"), state.getBlock());
        assertEquals(1, state.getProperties().size());
        assertTrue(state.getProperty("power").canBeInt());
        assertEquals(10, state.getProperty("power").getAsInt());

        state = builder.setBlock(dirt).setProperty("power", 10).setProperty("dirty", true).setProperty("facing", "north").build();
        assertEquals(3, state.getProperties().size());
        assertEquals("north", state.getProperty("facing").getAsString());
        assertTrue(state.getProperty("dirty").canBeBoolean());
        assertTrue(state.getProperty("dirty").getAsBoolean());
        assertTrue(state.getProperty("power").canBeInt());
        assertEquals(10, state.getProperty("power").getAsInt());

        builder.setBlock(dirt).reset().setProperty("test", "test");
        assertThrows(IllegalStateException.class, builder::build);

        state = builder.setFrom(state).setProperty("facing", "south").build();
        assertEquals(3, state.getProperties().size());
        assertEquals("south", state.getProperty("facing").getAsString());
        assertTrue(state.getProperty("dirty").canBeBoolean());
        assertTrue(state.getProperty("dirty").getAsBoolean());
        assertTrue(state.getProperty("power").canBeInt());
        assertEquals(10, state.getProperty("power").getAsInt());
    }

    @Test
    void testStringProperty() {
        BlockStateBuilder builder = BlockStateBuilder.get();
        BlockState state = builder.setProperty("name", "value").setBlock(new Identifier("minecraft", "dirt")).build();
        BlockPropertyValue value = state.getProperty("name");
        assertFalse(value.canBeBoolean());
        assertFalse(value.canBeInt());
        assertEquals("value", value.getAsString());
        assertThrows(UnsupportedOperationException.class, value::getAsBoolean);
        assertThrows(UnsupportedOperationException.class, value::getAsInt);
    }

    @Test
    void testIntProperty() {
        BlockStateBuilder builder = BlockStateBuilder.get();
        BlockState state = builder.setProperty("name", 42).setBlock(new Identifier("minecraft", "dirt")).build();
        BlockPropertyValue value = state.getProperty("name");
        assertFalse(value.canBeBoolean());
        assertTrue(value.canBeInt());
        assertEquals("42", value.getAsString());
        assertThrows(UnsupportedOperationException.class, value::getAsBoolean);
        assertEquals(42, value.getAsInt());
    }

    @Test
    void testBoolProperty() {
        BlockStateBuilder builder = BlockStateBuilder.get();
        BlockState state = builder.setProperty("name", true).setBlock(new Identifier("minecraft", "dirt")).build();
        BlockPropertyValue value = state.getProperty("name");
        assertTrue(value.canBeBoolean());
        assertFalse(value.canBeInt());
        assertEquals("true", value.getAsString());
        assertThrows(UnsupportedOperationException.class, value::getAsInt);
        assertTrue(value.getAsBoolean());
    }

    @Test
    void testBlockState() {
        BlockStateBuilder builder = BlockStateBuilder.get();
        BlockState state = builder.setBlock(new Identifier("minecraft", "dirt")).build();
        assertEquals(0, state.getProperties().size());

        state = state.withProperty("facing", "south");
        assertEquals(new Identifier("minecraft", "dirt"), state.getBlock());
        assertEquals(1, state.getProperties().size());
        assertEquals("south", state.getProperty("facing").getAsString());

        state = state.withProperty("dirty", true);
        assertEquals(new Identifier("minecraft", "dirt"), state.getBlock());
        assertEquals(2, state.getProperties().size());
        assertTrue(state.getProperty("dirty").canBeBoolean());
        assertTrue(state.getProperty("dirty").getAsBoolean());

        state = state.withProperty("power", 10);
        assertEquals(3, state.getProperties().size());
        assertEquals("south", state.getProperty("facing").getAsString());
        assertTrue(state.getProperty("dirty").canBeBoolean());
        assertTrue(state.getProperty("dirty").getAsBoolean());
        assertTrue(state.getProperty("power").canBeInt());
        assertEquals(10, state.getProperty("power").getAsInt());
    }

}
