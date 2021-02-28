package net.buildtheearth.terraplusplus.config.scalarparse.d;

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
 * Parses a square grid of {@code double} values from a binary representation.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize(using = DoubleScalarParser.Deserializer.class)
@JsonSerialize(using = DoubleScalarParser.Serializer.class)
@FunctionalInterface
public interface DoubleScalarParser {
    double[] parse(int resolution, @NonNull ByteBuf buffer) throws IOException;

    class Deserializer extends TypedDeserializer<DoubleScalarParser> {
        @Override
        protected Map<String, Class<? extends DoubleScalarParser>> registry() {
            return GlobalParseRegistries.SCALAR_PARSERS_DOUBLE;
        }
    }

    class Serializer extends TypedSerializer<DoubleScalarParser> {
        @Override
        protected Map<Class<? extends DoubleScalarParser>, String> registry() {
            return GlobalParseRegistries.SCALAR_PARSERS_DOUBLE.inverse();
        }
    }
}
