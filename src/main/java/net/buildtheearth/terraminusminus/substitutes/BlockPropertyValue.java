package net.buildtheearth.terraminusminus.substitutes;

/**
 * The value of a block property.
 */
public interface BlockPropertyValue {

    String getAsString();

    default boolean canBeInt() {
        return false;
    }

    default int getAsInt() {
        throw new UnsupportedOperationException("Block property \"" + this.getAsString() + "\" cannot be an integer.");
    }

    default boolean canBeBoolean() {
        return false;
    }

    default boolean getAsBoolean() {
        throw new UnsupportedOperationException("Block property \"" + this.getAsString() + "\" cannot be a boolean.");
    }

}