package net.buildtheearth.terraplusplus.util;

/**
 * Represents a type which can be interned.
 *
 * @author DaPorkchop_
 */
public interface Internable<I extends Internable<I>> {
    /**
     * Returns a canonical representation for this object.
     *
     * @return an object that has the same contents as this object, but is guaranteed to be from a pool of unique objects
     * @see String#intern()
     */
    I intern();
}
