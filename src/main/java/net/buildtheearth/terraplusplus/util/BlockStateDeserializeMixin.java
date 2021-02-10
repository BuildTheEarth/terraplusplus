package net.buildtheearth.terraplusplus.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.daporkchop.lib.common.function.PFunctions;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize(builder = BlockStateDeserializeMixin.Builder.class)
public abstract class BlockStateDeserializeMixin {
    protected static class Builder {
        protected final String id;
        protected final Map<String, String> properties;

        @JsonCreator
        public Builder(@NonNull String id) {
            this.id = id;
            this.properties = Collections.emptyMap();
        }

        @JsonCreator
        public Builder(
                @JsonProperty(value = "id", required = true) @NonNull String id,
                @JsonProperty("properties") Map<String, String> properties) {
            this.id = id;
            this.properties = PorkUtil.fallbackIfNull(properties, Collections.emptyMap());
        }

        public IBlockState build() {
            IBlockState state = Block.REGISTRY.getObject(new ResourceLocation(this.id)).getDefaultState();
            Map<String, IProperty<?>> lookup = state.getPropertyKeys().stream().collect(Collectors.toMap(IProperty::getName, PFunctions.identity()));
            for (Map.Entry<String, String> entry : this.properties.entrySet()) {
                IProperty<?> property = lookup.get(entry.getKey());
                state = state.withProperty(property, uncheckedCast(property.parseValue(entry.getValue()).orNull()));
            }
            return state;
        }
    }
}
