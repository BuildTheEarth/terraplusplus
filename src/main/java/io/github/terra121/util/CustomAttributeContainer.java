package io.github.terra121.util;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Map;

import static net.daporkchop.lib.common.util.PValidation.*;

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
     * @param key the key of the attribute to get
     * @return the attribute
     */
    public T getCustom(@NonNull String key) {
        T value = this.custom.get(key);
        checkArg(value != null, "unknown attribute key: \"%s\"!", key);
        return value;
    }
}
