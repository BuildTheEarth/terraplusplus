package net.buildtheearth.terraminusminus.substitutes;

import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.*;
import static java.util.Collections.*;
import static net.buildtheearth.terraminusminus.util.Strings.split;

public final class BlockStateBuilder {

    private Identifier block;
    private Map<String, BlockPropertyValue> properties = new HashMap<>();
    private static final Cached<BlockStateBuilder> instances = Cached.threadLocal(BlockStateBuilder::new, ReferenceStrength.SOFT);
    private static final Pattern SERIALIZED_REGEX = Pattern.compile(
            "^(?<identifier>[a-z0-9_.-]+(:?:[a-z0-9/_.-]+)?)"
            + "(?:\\[(?<properties>(?:[a-z0-9_]+=[a-z0-9_]+,?)*)])?$",
            Pattern.CASE_INSENSITIVE
    );

    public static BlockStateBuilder get() {
        return instances.get();
    }

    public BlockStateBuilder setBlock(Identifier name) {
        this.block = name;
        return this;
    }

    public BlockStateBuilder setProperty(String property, String value) {
        if (property == null || property.isEmpty()) throw new IllegalArgumentException("Property name cannot be null or empty.");
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("Property value cannot be null or empty.");
        this.properties.put(property, new StringPropertyValue(value));
        return this;
    }

    public BlockStateBuilder setProperty(String property, int value) {
        if (property == null || property.isEmpty()) throw new IllegalArgumentException("Property name cannot be null or empty.");
        this.properties.put(property, new IntPropertyValue(value));
        return this;
    }

    public BlockStateBuilder setProperty(String property, boolean value) {
        if (property == null || property.isEmpty()) throw new IllegalArgumentException("Property name cannot be null or empty.");
        this.properties.put(property, new BoolPropertyValue(value));
        return this;
    }

    public BlockStateBuilder setFrom(BlockState state) {
        this.block = state.getBlock();
        this.properties = new HashMap<>(state.getProperties());
        return this;
    }

    /**
     * Sets values in this {@link BlockStateBuilder builder} by parsing a serialized {@link BlockState} string.
     *
     * @param serialized the serialized {@link BlockState}, in the Minecraft command syntax
     * @return this {@link BlockStateBuilder builder}
     *
     * @throws IllegalArgumentException if the supplied string is not a valid serialized {@link BlockState}
     */
    public BlockStateBuilder setFromSerializedString(@NotNull String serialized) {
        Matcher matcher = SERIALIZED_REGEX.matcher(serialized);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid serialized block state: " + serialized);
        }

        Identifier identifier = Identifier.parse(matcher.group("identifier"));
        String[] properties;
        BlockPropertyValue[] propertyValues;

        String propertyGroup = matcher.group("properties");
        if (propertyGroup != null && !propertyGroup.isEmpty()) {
            String[] propertiesParts = split(propertyGroup, ',');
            properties = new String[propertiesParts.length];
            propertyValues = new BlockPropertyValue[propertiesParts.length];
            for (int i = 0; i < propertiesParts.length; i++) {
                String[] parts = propertiesParts[i].split("=");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid properties in serialized block state: " + serialized);
                }
                properties[i] = parts[0];
                propertyValues[i] = parsePropertyValues(parts[1]);
            }
        } else {
            properties = new  String[0];
            propertyValues = new BlockPropertyValue[0];
        }

        this.setBlock(identifier);
        for (int i = 0; i < properties.length; i++) {
            String name = properties[i];
            BlockPropertyValue value = propertyValues[i];
            this.properties.put(name, value);
        }

        return this;
    }

    private static BlockPropertyValue parsePropertyValues(String valueString) {
        try {
            int value = parseInt(valueString);
            return new IntPropertyValue(value);
        } catch (NumberFormatException ignored) { }
        if ("true".equalsIgnoreCase(valueString)) {
            return new BoolPropertyValue(true);
        }
        if ("false".equalsIgnoreCase(valueString)) {
            return new BoolPropertyValue(false);
        }
        return new StringPropertyValue(valueString);
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

    static class BlockStateImplementation implements BlockState {

        private final Identifier block;
        private final Map<String, BlockPropertyValue> properties;

        Object bukkitBlockData;

        public BlockStateImplementation(Identifier block, Map<String, BlockPropertyValue> properties) {
            this.block = block;
            this.properties = properties;
        }

        @Override
        public Identifier getBlock() {
            return this.block;
        }

        @Override
        public BlockPropertyValue getProperty(String property) {
            return this.properties.get(property);
        }

        @Override
        public boolean hasProperty(String property) {
            return this.properties.containsKey(property);
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

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            BlockStateImplementation that = (BlockStateImplementation) o;

            if (!block.equals(that.block))
                return false;
            return properties.equals(that.properties);
        }

        @Override
        public int hashCode() {
            int result = block.hashCode();
            result = 31 * result + properties.hashCode();
            return result;
        }

    }

    private static final class StringPropertyValue implements BlockPropertyValue {
        public final String value;

        public StringPropertyValue(String value) {
            this.value = value;
        }

        @Override
        public String getAsString() {
            return this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;
            StringPropertyValue that = (StringPropertyValue) o;
            return Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }

    }

    private static final class IntPropertyValue implements BlockPropertyValue {
        public final int value;

        public IntPropertyValue(int value) {
            this.value = value;
        }

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

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;
            IntPropertyValue that = (IntPropertyValue) o;
            return this.value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }
    }

    private static final class BoolPropertyValue implements BlockPropertyValue {
        public final boolean value;

        public BoolPropertyValue(boolean value) {
            this.value = value;
        }

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

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;
            BoolPropertyValue that = (BoolPropertyValue) o;
            return this.value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }
    }

}
