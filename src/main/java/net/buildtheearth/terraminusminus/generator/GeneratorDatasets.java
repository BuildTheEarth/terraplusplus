package net.buildtheearth.terraminusminus.generator;

import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.util.CustomAttributeContainer;

/**
 * Wrapper class which contains all of the datasets used for generation.
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
