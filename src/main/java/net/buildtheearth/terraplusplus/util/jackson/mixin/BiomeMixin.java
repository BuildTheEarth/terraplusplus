package net.buildtheearth.terraplusplus.util.jackson.mixin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.NonNull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize(converter = BiomeMixin.DeserializeConverter.class)
@JsonSerialize(converter = BiomeMixin.SerializeConverter.class)
public abstract class BiomeMixin {
    protected static class DeserializeConverter extends StdConverter<String, Biome> {
        @Override
        public Biome convert(@NonNull String value) {
            ResourceLocation id = new ResourceLocation(value);
            checkArg(Biome.REGISTRY.containsKey(id), "unknown biome id: %s", id);
            return Biome.REGISTRY.getObject(id);
        }
    }

    protected static class SerializeConverter extends StdConverter<Biome, String> {
        @Override
        public String convert(Biome value) {
            return value.getRegistryName().toString();
        }
    }
}
