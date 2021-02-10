package net.buildtheearth.terraplusplus.generator;

import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.util.CustomAttributeContainer;
import lombok.Getter;
import lombok.NonNull;

/**
 * Wrapper class which contains all of the datasets used by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
@Getter
public class GeneratorDatasets extends CustomAttributeContainer {
    protected final GeographicProjection projection;

    public GeneratorDatasets(@NonNull EarthGeneratorSettings settings) {
        super(EarthGeneratorPipelines.datasets(settings));

        this.projection = settings.projection();
    }
}
