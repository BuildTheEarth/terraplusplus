package net.buildtheearth.terraplusplus.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.buildtheearth.terraplusplus.TerraConstants;

import java.io.IOException;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
public abstract class TypedSerializer<T> extends JsonSerializer<T> {
    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        String name = this.registry().get(value.getClass());
        if (name == null) {
            throw JsonMappingException.from(gen, "invalid value type: " + value.getClass());
        }

        gen.writeStartObject(value);
        gen.writeFieldName(name);

        TerraConstants.JSON_MAPPER.rebuild().addMixIn(value.getClass(), MixIn.class).build().writeValue(gen, value);

        gen.writeEndObject();
    }

    protected abstract Map<Class<? extends T>, String> registry();

    @JsonSerialize
    private class MixIn {} // Have this here to avoid a bug in forge gradle (https://forums.minecraftforge.net/topic/40670-build-terminates-at-extractrangemapreplacedmain/)

}
