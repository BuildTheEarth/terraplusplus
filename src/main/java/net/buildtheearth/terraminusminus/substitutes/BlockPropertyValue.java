package net.buildtheearth.terraminusminus.substitutes;

/**
 * The value of a block property.
 *
 * @author SmylerMC
 */
public interface BlockPropertyValue {

    /**
     * @return this block property value as a String
     */
    String getAsString();

    /**
     * @return whether this value can be queried as an integer
     */
    default boolean canBeInt() {
        return false;
    }

    /**
     * @return this block property value as an integer
     * @throws UnsupportedOperationException if this value cannot be converted to an integer
     */
    default int getAsInt() {
        throw new UnsupportedOperationException("Block property \"" + this.getAsString() + "\" cannot be an integer.");
    }

    /**
     * @return whether this value can be queried as a boolean
     */
    default boolean canBeBoolean() {
        return false;
    }

    /**
     * @return this block property value as a boolean
     * @throws UnsupportedOperationException if this value cannot be converted to a boolean
     */
    default boolean getAsBoolean() {
        throw new UnsupportedOperationException("Block property \"" + this.getAsString() + "\" cannot be a boolean.");
    }

}