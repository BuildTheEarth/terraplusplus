package net.buildtheearth.terraminusminus.util;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Map;

import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor
public abstract class CustomAttributeContainer {
    @NonNull
    protected final Map<String, Object> custom;

    /**
     * Gets the custom attribute with the given key.
     *
     * @param key the key of the attribute to get
     * @return the attribute
     * @throws IllegalArgumentException if a property with the given name couldn't be found
     */
    public <T> T getCustom(@NonNull String key) {
        T value = uncheckedCast(this.custom.get(key));
        checkArg(value != null || this.custom.containsKey(key), "unknown property: \"%s\"", key);
        return value;
    }

    /**
     * Gets the custom attribute with the given key.
     *
     * @param key      the key of the attribute to get
     * @param fallback the value to return if the key couldn't be found
     * @return the attribute
     */
    public <T> T getCustom(@NonNull String key, T fallback) {
        return uncheckedCast(this.custom.getOrDefault(key, fallback));
    }
}
