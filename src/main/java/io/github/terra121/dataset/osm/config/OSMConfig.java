package io.github.terra121.dataset.osm.config;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.terra121.dataset.geojson.geometry.LineString;
import io.github.terra121.dataset.geojson.geometry.Polygon;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;

import static io.github.terra121.TerraConstants.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(OSMConfig.Adapter.class)
@Getter
@Builder
public final class OSMConfig {
    @NonNull
    protected final OSMMapper<LineString>[] line;
    @NonNull
    protected final OSMMapper<Polygon>[] polygon;

    static final class Adapter extends BetterTypeAdapter<OSMConfig> {
        @Override
        public void write(JsonWriter out, OSMConfig value) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public OSMConfig read(JsonReader in) throws IOException {
            OSMConfigBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "line":
                        builder.line(uncheckedCast(readList(in, new OSMMapper.LineAdapter()::read).toArray(new OSMMapper[0])));
                        break;
                    case "polygon":
                        builder.line(uncheckedCast(readList(in, new OSMMapper.PolygonAdapter()::read).toArray(new OSMMapper[0])));
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
