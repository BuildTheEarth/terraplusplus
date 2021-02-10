package io.github.terra121.generator;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.CustomAttributeContainer;
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
