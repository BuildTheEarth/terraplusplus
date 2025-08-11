package net.buildtheearth.terraminusminus.substitutes;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A block state is a block and all its properties when placed in a world.
 * This implementation of block states is rather simple and abstract,
 * in order to remain as independent as possible from any actual implementation.
 * <p>
 * A block state is an immutable object that contains the identifier of the base block,
 * as well as its properties and their values.
 *
 * @author Smyler
 */
public interface BlockState {

    /**
     * @return the identifier of the base block of that state
     */
    Identifier getBlock();

    /**
     * Gets the value of a given property of this state.
     *
     * @param property  the name of the property to get
     *
     * @return the property's value, or null if it doesn't have the given property
     */
    BlockPropertyValue getProperty(String property);

    /**
     * Tests whether this state has a given property set.
     *
     * @param property the property name
     *
     * @return <code>true</code> if the state has the given property set, <code>false</code> otherwise
     */
    boolean hasProperty(String property);

    /**
     * Gets a copy of this block state, with the given property applied.
     *
     * @param property  the name of the property to add or change
     * @param value     the value for the property
     *
     * @return the resulting block state
     */
    BlockState withProperty(String property, String value);

    /**
     * Gets a copy of this block state, with the given property applied.
     *
     * @param property  the name of the property to add or change
     * @param value     the value for the property
     *
     * @return the resulting block state
     */
    BlockState withProperty(String property, int value);

    /**
     * Gets a copy of this block state, with the given property applied.
     *
     * @param property  the name of the property to add or change
     * @param value     the value for the property
     *
     * @return the resulting block state
     */
    BlockState withProperty(String property, boolean value);

    /**
     * Gets the properties of this state as a read only map.
     *
     * @return the map of properties names and values for this state
     */
    Map<String, BlockPropertyValue> getProperties();

    /**
     * @return a color that describes this state, to use when rendering a mpa, for example
     */
    default int getPreviewColor() {
        return 0xFFFFFFFF;
    }

    /**
     * Parses a serialized {@link BlockState} from a {@link String}.
     *
     * @param string a serialized {@link BlockState} as it would be used in Minecraft commands
     * @return the parsed {@link BlockState}
     *
     * @throws IllegalArgumentException if the supplied String is not a valid serialied {@link BlockState}
     */
    static BlockState parse(@NotNull String string) {
        return BlockStateBuilder.get()
                .reset()
                .setFromSerializedString(string)
                .build();
    }
    
}