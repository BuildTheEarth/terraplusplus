package io.github.terra121.dataset.osm.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.osm.config.match.MatchCondition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.daporkchop.lib.common.function.PFunctions;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.terra121.TerraConstants.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * Parses block states.
 *
 * @author DaPorkchop_
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockStateParser extends JsonParser<IBlockState> {
    public static final BlockStateParser INSTANCE = new BlockStateParser();

    @Override
    public IBlockState read(JsonReader in) throws IOException {
        ResourceLocation id = null;
        Map<String, String> properties = Collections.emptyMap();

        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT) {
            String name = in.nextName();
            switch (name) {
                case "id":
                    id = new ResourceLocation(in.nextString());
                    break;
                case "properties": {
                    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
                    in.beginObject();
                    while (in.peek() != JsonToken.END_OBJECT) {
                        builder.put(in.nextName(), in.nextString());
                    }
                    in.endObject();
                    properties = builder.build();
                    break;
                }
                default:
                    throw new IllegalStateException("invalid property: " + name);
            }
        }
        in.endObject();

        IBlockState state = Block.REGISTRY.getObject(id).getDefaultState();
        Map<String, IProperty<?>> lookup = state.getPropertyKeys().stream().collect(Collectors.toMap(IProperty::getName, PFunctions.identity()));
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            IProperty<?> property = lookup.get(entry.getKey());
            state = state.withProperty(property, uncheckedCast(property.parseValue(entry.getValue()).orNull()));
        }
        return state;
    }
}
