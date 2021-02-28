package net.buildtheearth.terraplusplus.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.util.OrderedRegistry;
import net.daporkchop.lib.common.util.GenericMatcher;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IGenericEvent;

/**
 * Fired when an {@link OrderedRegistry} is being initialized.
 * <p>
 * This event is fired on {@link MinecraftForge#TERRAIN_GEN_BUS}.
 *
 * @author DaPorkchop_
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class InitEarthRegistryEvent<T> extends Event implements IGenericEvent<T> {
    @NonNull
    protected final EarthGeneratorSettings settings;
    @NonNull
    protected OrderedRegistry<T> registry;

    @Accessors(fluent = false)
    protected final Class<T> genericType = GenericMatcher.uncheckedFind(this.getClass(), InitEarthRegistryEvent.class, "T");
}
