package io.github.terra121.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import net.daporkchop.lib.common.util.PorkUtil;

import java.io.IOException;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
public abstract class TypedDeserializer<T> extends JsonDeserializer<T> {
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String name = p.nextFieldName();
        if (name == null) {
            throw JsonMappingException.from(p, "expected type name, found: " + p.currentToken());
        }

        Class<? extends T> clazz = this.registry().get(name);
        if (clazz == null) {
            throw JsonMappingException.from(p, "invalid type type name: " + name);
        }

        JsonToken token = p.nextToken();
        if (!clazz.isAnnotationPresent(SingleProperty.class) && token != JsonToken.START_OBJECT) {
            throw JsonMappingException.from(p, "expected json object, but found: " + token);
        }

        T value = ctxt.readValue(p, PorkUtil.<Class<? extends T>>uncheckedCast(clazz));

        token = p.nextToken();
        if (token != JsonToken.END_OBJECT) {
            throw JsonMappingException.from(p, "expected json object end, but found: " + token);
        }

        return value;
    }

    protected abstract Map<String, Class<? extends T>> registry();
}
