package net.buildtheearth.terraplusplus.config.scalarparse.i;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.config.TypedDeserializer;
import net.buildtheearth.terraplusplus.config.TypedSerializer;

import java.io.IOException;
import java.util.Map;

/**
 * Parses a square grid of {@code int} values from a binary representation.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize(using = IntScalarParser.Deserializer.class)
@JsonSerialize(using = IntScalarParser.Serializer.class)
@FunctionalInterface
public interface IntScalarParser {
    int[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException;

    class Deserializer extends TypedDeserializer<IntScalarParser> {
        @Override
        protected Map<String, Class<? extends IntScalarParser>> registry() {
            return GlobalParseRegistries.SCALAR_PARSERS_INT;
        }
    }

    class Serializer extends TypedSerializer<IntScalarParser> {
        @Override
        protected Map<Class<? extends IntScalarParser>, String> registry() {
            return GlobalParseRegistries.SCALAR_PARSERS_INT.inverse();
        }
    }
}
