package net.buildtheearth.terraplusplus.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public class IntListDeserializer extends JsonDeserializer<int[]> {
    @Override
    public int[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonMapper mapper = (JsonMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node.isNumber()) {
            return new int[]{ node.asInt() };
        } else if (node.isArray()) {
            return StreamSupport.stream(node.spliterator(), false)
                    .mapToInt(n -> {
                        checkArg(n.isNumber(), "not an int: %s", n);
                        return n.asInt();
                    })
                    .distinct().sorted().toArray();
        } else if (node.isObject()) {
            IntRange range = mapper.treeToValue(node, IntRange.class);
            return IntStream.rangeClosed(range.min(), range.max()).toArray();
        }

        throw new IllegalArgumentException(node.toString());
    }
}
