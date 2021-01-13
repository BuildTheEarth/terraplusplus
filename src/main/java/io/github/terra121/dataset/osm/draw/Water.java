package io.github.terra121.dataset.osm.draw;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.osm.config.JsonParser;
import io.github.terra121.generator.cache.CachedChunkData;
import lombok.Builder;
import lombok.NonNull;

import java.io.IOException;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;

/**
 * {@link DrawFunction} which updates the water depth based on the pixel weight.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Water.Parser.class)
@Builder
final class Water implements DrawFunction {
    @Builder.Default
    protected final int minDepth = Integer.MIN_VALUE;
    @Builder.Default
    protected final int maxDepth = Integer.MAX_VALUE;

    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        weight = clamp(weight, this.minDepth, this.maxDepth);
        int waterDepth = data.getExtra(x, z, CachedChunkData.EXTRA_WATERDEPTH);
        if (weight > waterDepth) {
            data.setExtra(x, z, CachedChunkData.EXTRA_WATERDEPTH, weight);
        }
    }

    static class Parser extends JsonParser<Water> {
        @Override
        public Water read(JsonReader in) throws IOException {
            WaterBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "minDepth":
                        builder.minDepth(in.nextInt());
                        break;
                    case "maxDepth":
                        builder.maxDepth(in.nextInt());
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
