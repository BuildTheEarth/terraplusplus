package io.github.terra121.dataset.vector.draw;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.osm.JsonParser;
import io.github.terra121.generator.CachedChunkData;
import lombok.Builder;
import lombok.NonNull;

import java.io.IOException;

import static io.github.terra121.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(WeightAdd.Parser.class)
@Builder
final class WeightAdd implements DrawFunction {
    @NonNull
    protected final DrawFunction delegate;
    protected final int value;

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        this.delegate.drawOnto(data, x, z, weight + this.value);
    }

    static class Parser extends JsonParser<WeightAdd> {
        @Override
        public WeightAdd read(JsonReader in) throws IOException {
            WeightAddBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "value":
                        builder.value(in.nextInt());
                        break;
                    case "delegate":
                        in.beginObject();
                        builder.delegate(GSON.fromJson(in, DrawFunction.class));
                        in.endObject();
                        break;
                    default:
                        throw new IllegalStateException("invalid property: " + name);
                }
            }
            in.endObject();

            return builder.build();
        }
    }
}