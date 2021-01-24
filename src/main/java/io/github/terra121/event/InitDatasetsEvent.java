package io.github.terra121.event;

import io.github.terra121.generator.EarthGeneratorSettings;
import io.github.terra121.generator.GeneratorDatasets;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraftforge.common.MinecraftForge;

/**
 * Fired on {@link MinecraftForge#TERRAIN_GEN_BUS} when a new instance of {@link GeneratorDatasets} is being constructed.
 * <p>
 * Allows custom datasets to be registered.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
public class InitDatasetsEvent extends AbstractCustomRegistrationEvent<Object> {
    @NonNull
    protected final EarthGeneratorSettings settings;

    @Override
    public void register(@NonNull String key, @NonNull Object value) {
        super.register(key, value);
    }

    @Override
    public void remove(@NonNull String key) {
        super.remove(key);
    }

    @Override
    public Object get(@NonNull String key) {
        return super.get(key);
    }
}
