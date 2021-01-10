package io.github.terra121.dataset.osm.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.NonNull;
import net.daporkchop.lib.common.function.io.IOFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author DaPorkchop_
 */
abstract class BetterTypeAdapter<T> extends TypeAdapter<T> {
    static <T> List<T> readList(@NonNull JsonReader in, @NonNull IOFunction<JsonReader, T> elementParser) throws IOException {
        List<T> list = new ArrayList<>();

        in.beginArray();
        while (in.peek() != JsonToken.END_ARRAY) {
            list.add(elementParser.applyThrowing(in));
        }
        in.endArray();

        return list;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        throw new UnsupportedOperationException();
    }
}
