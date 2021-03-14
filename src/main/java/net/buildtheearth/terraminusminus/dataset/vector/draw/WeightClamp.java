package net.buildtheearth.terraminusminus.dataset.vector.draw;

import java.io.IOException;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import lombok.Builder;
import lombok.NonNull;
import net.buildtheearth.terraminusminus.TerraConstants;
import net.buildtheearth.terraminusminus.dataset.osm.JsonParser;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.daporkchop.lib.common.math.PMath;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(WeightClamp.Parser.class)
@Builder
final class WeightClamp implements DrawFunction {
    @NonNull
    protected final DrawFunction delegate;
    @Builder.Default
    protected final int min = Integer.MIN_VALUE;
    @Builder.Default
    protected final int max = Integer.MAX_VALUE;

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        this.delegate.drawOnto(data, x, z, PMath.clamp(weight, this.min, this.max));
    }

    static class Parser extends JsonParser<WeightClamp> {
        @Override
        public WeightClamp read(JsonReader in) throws IOException {
            WeightClampBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "min":
                        builder.min(in.nextInt());
                        break;
                    case "max":
                        builder.max(in.nextInt());
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
