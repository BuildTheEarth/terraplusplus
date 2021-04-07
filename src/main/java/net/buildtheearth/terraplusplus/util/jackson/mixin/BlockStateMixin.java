package net.buildtheearth.terraplusplus.util.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.function.PFunctions;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize(builder = BlockStateMixin.Builder.class)
@JsonSerialize(using = BlockStateMixin.Serializer.class)
public abstract class BlockStateMixin {
    @RequiredArgsConstructor
    @Getter(onMethod_ = { @JsonGetter })
    @JsonSerialize
    protected static class OnlyId {
        @NonNull
        protected final String id;
    }

    @Getter(onMethod_ = { @JsonGetter })
    @JsonSerialize
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

    protected static class Serializer extends JsonSerializer<IBlockState> {
        @Override
        public void serialize(IBlockState value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            String id = value.getBlock().getRegistryName().toString();
            Object o;
            if (value == value.getBlock().getDefaultState()) {
                o = new OnlyId(id);
            } else {
                o = new Builder(id, value.getProperties().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getName(), e -> e.getValue().toString())));
            }
            gen.writeObject(o);
        }
    }
}
