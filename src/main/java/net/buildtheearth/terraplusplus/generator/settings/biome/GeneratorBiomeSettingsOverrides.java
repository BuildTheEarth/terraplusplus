package net.buildtheearth.terraplusplus.generator.settings.biome;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.biome.IEarthBiomeFilter;
import net.buildtheearth.terraplusplus.generator.biome.UserOverrideBiomeFilter;

/**
 * Allows users to override biome generation in specific areas.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class GeneratorBiomeSettingsOverrides implements GeneratorBiomeSettings {
    protected final UserOverrideBiomeFilter.BiomeOverrideArea[] areas;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public GeneratorBiomeSettingsOverrides(
            @JsonProperty(value = "areas", required = true) @NonNull UserOverrideBiomeFilter.BiomeOverrideArea... areas) {
        this.areas = areas;
    }

    @Override
    public IEarthBiomeFilter<?> filter() {
        return new UserOverrideBiomeFilter(this.areas);
    }
}
