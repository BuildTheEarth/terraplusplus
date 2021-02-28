package net.buildtheearth.terraplusplus.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
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
public class InitDatasetsEvent extends AbstractCustomRegistrationEvent {
    @NonNull
    protected final EarthGeneratorSettings settings;
}
