package net.buildtheearth.terraminusminus.substitutes.exceptions;

import net.buildtheearth.terraminusminus.substitutes.Biome;
import net.buildtheearth.terraminusminus.substitutes.BlockState;
import net.buildtheearth.terraminusminus.substitutes.Identifier;

/**
 * Thrown when a problem is encountered working with content substitutes
 * (e.g. {@link Identifier}, {@link BlockState}, {@link Biome}, ...).
 *
 * @author Smyler
 */
public abstract class SubstituteException extends RuntimeException {

    public SubstituteException(String message) {
        super(message);
    }

}
