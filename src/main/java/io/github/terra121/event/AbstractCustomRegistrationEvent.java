package io.github.terra121.event;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.NonNull;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collections;
import java.util.Map;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractCustomRegistrationEvent extends Event {
    protected final Map<String, Object> custom = new Object2ObjectOpenHashMap<>();

    public void register(@NonNull String key, @NonNull Object value) {
        this.custom.put(key, value);
    }

    public void remove(@NonNull String key) {
        this.custom.remove(key);
    }

    public <T> T get(@NonNull String key) {
        return uncheckedCast(this.custom.get(key));
    }

    public Map<String, Object> getAllCustomProperties() {
        return this.custom.isEmpty() ? Collections.emptyMap() : ImmutableMap.copyOf(this.custom);
    }
}
