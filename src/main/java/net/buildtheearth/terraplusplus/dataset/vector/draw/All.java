package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(All.Parser.class)
@RequiredArgsConstructor
final class All implements DrawFunction {
    @NonNull
    protected final DrawFunction[] delegates;

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        for (DrawFunction delegate : this.delegates) {
            delegate.drawOnto(data, x, z, weight);
        }
    }

    static class Parser extends DrawFunctionParser {
        @Override
        public DrawFunction read(JsonReader in) throws IOException {
            return new All(readTypedList(in, this).toArray(new DrawFunction[0]));
        }
    }
}
