package io.github.terra121.util;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor
public abstract class CustomAttributeContainer<T> {
    @NonNull
    protected final Map<String, T> custom;

    /**
     * Gets the custom attribute with the given key.
     *
     * @param key      the key of the attribute to get
     * @param fallback the value to return if the key couldn't be found
     * @return the attribute
     */
    public T getCustom(@NonNull String key, T fallback) {
        return this.custom.getOrDefault(key, fallback);
    }
}
