package net.buildtheearth.terraminusminus.substitutes.exceptions;

import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraminusminus.substitutes.BlockState;

/**
 * Indicates failure in an attempt to translate a Terra-- substitute object into a foreign implementation
 * (e.g. failed to convert a Terra-- {@link BlockState} into a Bukkit BlockData).
 *
 * @author Smyler
 */
@Getter
public class TranslateToForeignObjectException extends SubstituteException {

    private final @NonNull Object substituteObject;
    private final @NonNull Class<?> foreignClass;

    public TranslateToForeignObjectException(@NonNull Object substituteObject, @NonNull Class<?> foreignClass, @NonNull String errorMessage) {
        super(
                "Failed to convert substitute object " + substituteObject
                + " of class " + substituteObject.getClass()
                + " into a substitute of class " + foreignClass
                + ": " + errorMessage
        );
        this.substituteObject = substituteObject;
        this.foreignClass = foreignClass;
    }

}
