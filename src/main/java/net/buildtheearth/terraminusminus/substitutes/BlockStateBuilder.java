package net.buildtheearth.terraminusminus.substitutes;

import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.*;

public final class BlockStateBuilder {

    private Identifier block;
    private Map<String, BlockPropertyValue> properties = new HashMap<>();
    private static final Cached<BlockStateBuilder> instances = Cached.threadLocal(BlockStateBuilder::new, ReferenceStrength.SOFT);

    public static BlockStateBuilder get() {
        return instances.get();
    }

    public BlockStateBuilder setBlock(Identifier name) {
        this.block = name;
        return this;
    }

    public BlockStateBuilder setProperty(String property, String value) {
        if (property == null || property.length() == 0) throw new IllegalArgumentException("Property name cannot be null or empty.");
        if (value == null || value.length() == 0) throw new IllegalArgumentException("Property value cannot be null or empty.");
        this.properties.put(property, new StringPropertyValue(value));
        return this;
    }

    public BlockStateBuilder setProperty(String property, int value) {
        if (property == null || property.length() == 0) throw new IllegalArgumentException("Property name cannot be null or empty.");
        this.properties.put(property, new IntPropertyValue(value));
        return this;
    }

    public BlockStateBuilder setProperty(String property, boolean value) {
        if (property == null || property.length() == 0) throw new IllegalArgumentException("Property name cannot be null or empty.");
        this.properties.put(property, new BoolPropertyValue(value));
        return this;
    }

    public BlockStateBuilder setFrom(BlockState state) {
        this.block = state.getBlock();
        this.properties = new HashMap<>(state.getProperties());
        return this;
    }

    public BlockState build() {
        if (this.block == null) throw new IllegalStateException("Trying to build a block state, but no block is set!");
        BlockStateImplementation state = new BlockStateImplementation(this.block, unmodifiableMap(this.properties));
        this.reset();
        return state;
    }

    public BlockStateBuilder reset() {
        this.block = null;
        this.properties = new HashMap<>();
        return this;
    }

    private record BlockStateImplementation(Identifier block, Map<String, BlockPropertyValue> properties) implements BlockState {

        @Override
        public Identifier getBlock() {
            return this.block;
        }

        @Override
        public BlockPropertyValue getProperty(String property) {
            return this.properties.get(property);
        }

        @Override
        public BlockState withProperty(String property, String value) {
            return BlockStateBuilder.get().setFrom(this).setProperty(property, value).build();
        }

        @Override
        public BlockState withProperty(String property, int value) {
            return BlockStateBuilder.get().setFrom(this).setProperty(property, value).build();
        }

        @Override
        public BlockState withProperty(String property, boolean value) {
            return BlockStateBuilder.get().setFrom(this).setProperty(property, value).build();
        }

        @Override
        public Map<String, BlockPropertyValue> getProperties() {
            return this.properties;
        }

    }

    private record StringPropertyValue(String value) implements BlockPropertyValue {

        @Override
        public String getAsString() {
            return this.value;
        }

    }

    private record IntPropertyValue(int value) implements BlockPropertyValue {

        @Override
        public String getAsString() {
            return "" + this.value;
        }

        @Override
        public boolean canBeInt() {
            return true;
        }

        @Override
        public int getAsInt() {
            return this.value;
        }

    }

    private record BoolPropertyValue(boolean value) implements BlockPropertyValue {

        @Override
        public String getAsString() {
            return "" + this.value;
        }

        @Override
        public boolean canBeBoolean() {
            return true;
        }

        @Override
        public boolean getAsBoolean() {
            return this.value;
        }

    }

}
