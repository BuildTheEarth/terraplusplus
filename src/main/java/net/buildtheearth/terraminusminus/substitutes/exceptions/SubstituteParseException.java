package net.buildtheearth.terraminusminus.substitutes.exceptions;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a conversion from a String to a substitute object fails.
 *
 * @author Smyler
 */
public class SubstituteParseException extends SubstituteException {

    public SubstituteParseException(@NotNull String serialized, @NonNull Class<?> clazz, @NonNull String message) {
        super("Failed to parse '" + serialized + "' into " + clazz + ": " + message);
    }

}
