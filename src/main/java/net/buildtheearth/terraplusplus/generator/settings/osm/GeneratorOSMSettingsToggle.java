package net.buildtheearth.terraplusplus.generator.settings.osm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(onConstructor_ = { @JsonCreator(mode = JsonCreator.Mode.DELEGATING) })
@Getter(onMethod_ = { @JsonValue })
@JsonDeserialize
public final class GeneratorOSMSettingsToggle extends AbstractGeneratorOSMSettings {
    @NonNull
    protected final Map<Feature, Boolean> features;
}
