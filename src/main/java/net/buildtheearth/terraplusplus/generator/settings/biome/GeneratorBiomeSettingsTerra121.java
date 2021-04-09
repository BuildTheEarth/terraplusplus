package net.buildtheearth.terraplusplus.generator.settings.biome;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import net.buildtheearth.terraplusplus.generator.biome.IEarthBiomeFilter;
import net.buildtheearth.terraplusplus.generator.biome.Terra121BiomeFilter;

/**
 * Identical to Terra 1:1's biome generation, for backwards-compatibility.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class GeneratorBiomeSettingsTerra121 implements GeneratorBiomeSettings {
    @Override
    public IEarthBiomeFilter<?> filter() {
        return new Terra121BiomeFilter();
    }
}
