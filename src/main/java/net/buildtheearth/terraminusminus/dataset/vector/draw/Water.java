package net.buildtheearth.terraminusminus.dataset.vector.draw;

import java.io.IOException;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.dataset.osm.JsonParser;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;

/**
 * {@link DrawFunction} which updates the water depth based on the pixel weight.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Water.Parser.class)
final class Water implements DrawFunction {
    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        data.updateWaterDepth(x, z, weight);
    }

    static class Parser extends JsonParser<Water> {
        @Override
        public Water read(JsonReader in) throws IOException {
            in.beginObject();
            in.endObject();
            return new Water();
        }
    }
}
