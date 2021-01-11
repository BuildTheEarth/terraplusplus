package io.github.terra121.util;

/**
 * A function that accepts two {@code int} parameters and returns a {@code double}.
 *
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface IntToDoubleBiFunction {
    double apply(int x, int z);
}
