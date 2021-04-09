package net.buildtheearth.terraplusplus.generator.settings.biome;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.biome.ConstantBiomeFilter;
import net.buildtheearth.terraplusplus.generator.biome.IEarthBiomeFilter;
import net.minecraft.world.biome.Biome;

/**
 * Generates a single, fixed biome in the entire world.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class GeneratorBiomeSettingsConstant implements GeneratorBiomeSettings {
    protected final Biome biome;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public GeneratorBiomeSettingsConstant(
            @JsonProperty(value = "biome", required = true) @NonNull Biome biome) {
        this.biome = biome;
    }

    @Override
    public IEarthBiomeFilter<?> filter() {
        return new ConstantBiomeFilter(this.biome);
    }
}
