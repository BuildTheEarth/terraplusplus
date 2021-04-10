package net.buildtheearth.terraplusplus.generator.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.state.IBlockState;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author DaPorkchop_
 */
public final class GeneratorBlockSelector extends JsonDeserializer<Supplier<IBlockState>> {
    @Override
    public Supplier<IBlockState> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (p.currentToken() == JsonToken.START_OBJECT) {
            return ctxt.readValue(p, Single.class);
        } else if (p.currentToken() == JsonToken.START_ARRAY) {
            return ctxt.readValue(p, Multi.class);
        }
        throw new IllegalArgumentException(p.currentToken().name());
    }

    @RequiredArgsConstructor(onConstructor_ = { @JsonCreator(mode = JsonCreator.Mode.DELEGATING) })
    public static final class Single implements Supplier<IBlockState> {
        @NonNull
        protected final IBlockState state;

        @Override
        @JsonValue
        public IBlockState get() {
            return this.state;
        }
    }

    @RequiredArgsConstructor(onConstructor_ = { @JsonCreator(mode = JsonCreator.Mode.DELEGATING) })
    @Getter(onMethod_ = { @JsonValue })
    public static final class Multi implements Supplier<IBlockState> {
        @NonNull
        protected final IBlockState[] states;

        @Override
        public IBlockState get() {
            return this.states[ThreadLocalRandom.current().nextInt(this.states.length)];
        }
    }
}
