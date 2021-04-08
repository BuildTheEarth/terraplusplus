package net.buildtheearth.terraplusplus.generator.settings.osm;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import net.daporkchop.lib.common.function.PFunctions;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class GeneratorOSMSettingsDefault extends AbstractGeneratorOSMSettings {
    @Override
    protected Map<Feature, Boolean> features() {
        return Stream.of(Feature.values()).collect(Collectors.toMap(PFunctions.identity(), Feature::isDefault));
    }
}
