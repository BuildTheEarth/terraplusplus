package net.buildtheearth.terraminusminus.util;

import static net.daporkchop.lib.common.util.PValidation.checkArg;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.substitutes.Biome;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize(converter = BiomeDeserializeMixin.Converter.class)
public abstract class BiomeDeserializeMixin {
    protected static class Converter extends StdConverter<String, Biome> {
        @Override
        public Biome convert(@NonNull String value) {
            Biome biome = Biome.parse(value);
            checkArg(biome != null, "unknown biome id: %s", value);
            return biome;
        }
    }
}
