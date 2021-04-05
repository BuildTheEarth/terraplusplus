package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.Builder;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(WeightGreaterThan.Parser.class)
@Builder
final class WeightGreaterThan implements DrawFunction {
    @NonNull
    protected final DrawFunction delegate;
    protected final int value;

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        if (weight > this.value) {
            this.delegate.drawOnto(data, x, z, weight);
        }
    }

    static class Parser extends JsonParser<WeightGreaterThan> {
        @Override
        public WeightGreaterThan read(JsonReader in) throws IOException {
            WeightGreaterThanBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "value":
                        builder.value(in.nextInt());
                        break;
                    case "delegate":
                        in.beginObject();
                        builder.delegate(TerraConstants.GSON.fromJson(in, DrawFunction.class));
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
