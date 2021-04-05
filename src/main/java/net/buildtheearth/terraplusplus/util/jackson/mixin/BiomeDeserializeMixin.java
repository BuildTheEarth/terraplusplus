package net.buildtheearth.terraplusplus.util.jackson.mixin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.NonNull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize(converter = BiomeDeserializeMixin.Converter.class)
public abstract class BiomeDeserializeMixin {
    protected static class Converter extends StdConverter<String, Biome> {
        @Override
        public Biome convert(@NonNull String value) {
            ResourceLocation id = new ResourceLocation(value);
            checkArg(Biome.REGISTRY.containsKey(id), "unknown biome id: %s", id);
            return Biome.REGISTRY.getObject(id);
        }
    }
}
