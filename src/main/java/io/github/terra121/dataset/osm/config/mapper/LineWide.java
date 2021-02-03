package io.github.terra121.dataset.osm.config.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.osm.geojson.Geometry;
import io.github.terra121.dataset.osm.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.osm.config.JsonParser;
import io.github.terra121.dataset.osm.config.dvalue.DValue;
import io.github.terra121.dataset.osm.draw.DrawFunction;
import io.github.terra121.dataset.osm.element.Element;
import io.github.terra121.dataset.osm.element.line.WideLine;
import lombok.Builder;
import lombok.NonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static io.github.terra121.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(LineWide.Parser.class)
@Builder
final class LineWide implements LineMapper {
    @NonNull
    protected final DrawFunction draw;
    @NonNull
    protected final DValue layer;
    @NonNull
    protected final DValue radius;

    @Override
    public Collection<Element> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull MultiLineString projectedGeometry) {
        return Collections.singleton(new WideLine(id, this.layer.apply(tags), this.draw, projectedGeometry, this.radius.apply(tags)));
    }

    static final class Parser extends JsonParser<LineWide> {
        @Override
        public LineWide read(JsonReader in) throws IOException {
            LineWideBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "draw":
                        in.beginObject();
                        builder.draw(GSON.fromJson(in, DrawFunction.class));
                        in.endObject();
                        break;
                    case "layer":
                        in.beginObject();
                        builder.layer(GSON.fromJson(in, DValue.class));
                        in.endObject();
                        break;
                    case "radius":
                        in.beginObject();
                        builder.radius(GSON.fromJson(in, DValue.class));
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
