package io.github.terra121.event;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.NonNull;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collections;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractCustomRegistrationEvent<T> extends Event {
    protected final Map<String, T> custom = new Object2ObjectOpenHashMap<>();

    public void register(@NonNull String key, @NonNull T value) {
        this.custom.put(key, value);
    }

    public void remove(@NonNull String key) {
        this.custom.remove(key);
    }

    public T get(@NonNull String key) {
        return this.custom.get(key);
    }

    public Map<String, T> getAllCustomProperties() {
        return this.custom.isEmpty() ? Collections.emptyMap() : ImmutableMap.copyOf(this.custom);
    }
}
