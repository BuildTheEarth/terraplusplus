package io.github.terra121.dataset.osm.draw;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.osm.config.JsonParser;
import io.github.terra121.generator.cache.CachedChunkData;
import lombok.Builder;
import lombok.NonNull;
import net.daporkchop.lib.common.math.PMath;

import java.io.IOException;

import static io.github.terra121.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(Clamp.Parser.class)
@Builder
final class Clamp implements DrawFunction {
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

    static class Parser extends JsonParser<Clamp> {
        @Override
        public Clamp read(JsonReader in) throws IOException {
            ClampBuilder builder = builder();

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
