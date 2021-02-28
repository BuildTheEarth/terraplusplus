package net.buildtheearth.terraplusplus.dataset.osm.dvalue;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.Builder;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;

import java.io.IOException;
import java.util.Map;

/**
 * Returns a single, constant value.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Tag.Parser.class)
@Builder
final class Tag implements DValue {
    @NonNull
    protected final String key;
    protected final double fallback;

    @Override
    public double apply(@NonNull Map<String, String> tags) {
        String value = tags.get(this.key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return this.fallback;
    }

    static class Parser extends JsonParser<DValue> {
        @Override
        public DValue read(JsonReader in) throws IOException {
            TagBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "key":
                        builder.key(in.nextString().intern());
                        break;
                    case "fallback":
                        builder.fallback(in.nextDouble());
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
