package net.buildtheearth.terraplusplus.generator;

import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.util.CustomAttributeContainer;

/**
 * Wrapper class which contains all of the datasets used by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
@Getter
public class GeneratorDatasets extends CustomAttributeContainer {
    protected final EarthGeneratorSettings settings;
    protected final GeographicProjection projection;

    public GeneratorDatasets(@NonNull EarthGeneratorSettings settings) {
        super(EarthGeneratorPipelines.datasets(settings));

        this.settings = settings;
        this.projection = settings.projection();
    }
}
