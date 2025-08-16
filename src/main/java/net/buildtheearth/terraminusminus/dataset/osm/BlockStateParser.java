package net.buildtheearth.terraminusminus.dataset.osm;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.buildtheearth.terraminusminus.substitutes.BlockStateBuilder;
import net.buildtheearth.terraminusminus.substitutes.BlockState;
import net.buildtheearth.terraminusminus.substitutes.Identifier;

/**
 * Parses block states.
 *
 * @author DaPorkchop_, SmylerMC
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockStateParser extends JsonParser<BlockState> {

    public static final BlockStateParser INSTANCE = new BlockStateParser();

    @Override
    public BlockState read(JsonReader in) throws IOException {
        BlockStateBuilder builder  = BlockStateBuilder.get();
        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT) {
            String name = in.nextName();
            switch (name) {
                case "id":
                    builder.setBlock(Identifier.parse(in.nextString()));
                    break;
                case "properties":
                    in.beginObject();
                    while (in.peek() != JsonToken.END_OBJECT) {
                        String propertyName = in.nextName();
                        switch (in.peek()) {
                            case STRING:
                                builder.setProperty(propertyName, in.nextString());
                                break;
                            case NUMBER:
                                builder.setProperty(propertyName, in.nextInt());
                                break;
                            case BOOLEAN:
                                builder.setProperty(propertyName, in.nextBoolean());
                                break;
                            default:
                                throw new IllegalStateException("Invalid property type: " + in.peek());
                        }
                    }
                    in.endObject();
                    break;
                default:
                    throw new IllegalStateException("Invalid block state: " + name);
            }
        }
        in.endObject();
        return builder.build();
    }

}
