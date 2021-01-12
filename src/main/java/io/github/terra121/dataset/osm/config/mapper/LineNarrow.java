package io.github.terra121.dataset.osm.config.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.osm.Element;
import io.github.terra121.dataset.osm.config.JsonParser;
import lombok.Builder;
import lombok.NonNull;
import net.minecraft.block.state.IBlockState;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static io.github.terra121.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(LineNarrow.Parser.class)
@Builder
final class LineNarrow implements LineMapper {
    @NonNull
    protected final IBlockState block;
    protected final boolean crossWater;

    @Override
    public Collection<Element> apply(String id, @NonNull Map<String, String> tags, @NonNull MultiLineString geometry) {
        return null; //TODO
    }

    static final class Parser extends JsonParser<LineNarrow> {
        @Override
        public LineNarrow read(JsonReader in) throws IOException {
            LineNarrowBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "block":
                        builder.block(GSON.fromJson(in, IBlockState.class));
                        break;
                    case "crossWater":
                        builder.crossWater(in.nextBoolean());
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
