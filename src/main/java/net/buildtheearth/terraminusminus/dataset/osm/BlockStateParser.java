package net.buildtheearth.terraminusminus.dataset.osm;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.block.Block;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.block.properties.IProperty;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.block.state.IBlockState;
import net.buildtheearth.terraminusminus.substitutes.net.minecraft.util.ResourceLocation;
import net.daporkchop.lib.common.function.PFunctions;

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

        IBlockState state = Block.byResourceLocation(id).getDefaultState();
        Map<String, IProperty<?>> lookup = state.getPropertyKeys().stream().collect(Collectors.toMap(IProperty::getName, PFunctions.identity()));
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            IProperty<?> property = lookup.get(entry.getKey());
            //TODO
//            state = state.withProperty(property, uncheckedCast(property.parseValue(entry.getValue()).orNull()));
        }
        return state;
    }
}
