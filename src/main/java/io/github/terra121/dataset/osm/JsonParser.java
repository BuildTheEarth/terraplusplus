package io.github.terra121.dataset.osm;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.function.io.IOBiFunction;
import net.daporkchop.lib.common.function.io.IOFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.terra121.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Base implementation of {@link TypeAdapter} for deserialzing
 *
 * @author DaPorkchop_
 */
public abstract class JsonParser<T> extends TypeAdapter<T> {
    public static <T> List<T> readList(@NonNull JsonReader in, @NonNull IOFunction<JsonReader, T> elementParser) throws IOException {
        List<T> list = new ArrayList<>();

        in.beginArray();
        while (in.peek() != JsonToken.END_ARRAY) {
            list.add(elementParser.applyThrowing(in));
        }
        in.endArray();

        return list;
    }

    public static <T> List<T> readTypedList(@NonNull JsonReader in, @NonNull Class<T> type) throws IOException {
        List<T> list = new ArrayList<>();

        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT) {
            list.add(GSON.fromJson(in, type));
        }
        in.endObject();

        return list;
    }

    public static <T> List<T> readTypedList(@NonNull JsonReader in, @NonNull IOBiFunction<String, JsonReader, T> elementParser) throws IOException {
        List<T> list = new ArrayList<>();

        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT) {
            list.add(elementParser.applyThrowing(in.nextName(), in));
        }
        in.endObject();

        return list;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses values with named types.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    private static abstract class AbstractTyped<T, V> extends JsonParser<V> implements IOBiFunction<String, JsonReader, T> {
        @NonNull
        protected final String name;
        @NonNull
        protected final Map<String, Class<? extends T>> types;

        @Override
        public T applyThrowing(String type, JsonReader in) throws IOException {
            Class<? extends T> clazz = this.types.get(type);
            checkArg(clazz != null, "type \"%s\" is not supported by \"%s\"!", type, this.name);
            return GSON.fromJson(in, clazz);
        }
    }

    /**
     * Parses a single value with a named type.
     *
     * @author DaPorkchop_
     */
    public static abstract class Typed<T> extends AbstractTyped<T, T> {
        public Typed(String name, Map<String, Class<? extends T>> types) {
            super(name, types);
        }

        @Override
        public T read(JsonReader in) throws IOException {
            return this.applyThrowing(in.nextName(), in);
        }
    }
}
