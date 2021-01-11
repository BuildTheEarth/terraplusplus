package io.github.terra121.dataset.osm.config.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.osm.Generatable;
import io.github.terra121.dataset.osm.config.JsonParser;
import io.github.terra121.dataset.osm.config.dvalue.DValue;
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
@JsonAdapter(LineWide.Parser.class)
@Builder
final class LineWide implements LineMapper {
    @NonNull
    protected final IBlockState block;
    @NonNull
    protected final DValue width;
    protected final boolean crossWater;

    @Override
    public Collection<Generatable> apply(String id, @NonNull Map<String, String> tags, @NonNull MultiLineString geometry) {
        return null; //TODO
    }

    static final class Parser extends JsonParser<LineWide> {
        @Override
        public LineWide read(JsonReader in) throws IOException {
            LineWideBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "block":
                        builder.block(GSON.fromJson(in, IBlockState.class));
                        break;
                    case "width":
                        in.beginObject();
                        builder.width(GSON.fromJson(in, DValue.class));
                        in.endObject();
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
