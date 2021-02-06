package io.github.terra121.dataset.osm.config.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.osm.config.JsonParser;
import io.github.terra121.dataset.osm.config.dvalue.DValue;
import io.github.terra121.dataset.vector.draw.DrawFunction;
import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.dataset.vector.geometry.polygon.FillPolygon;
import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.dataset.geojson.geometry.MultiPolygon;
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
@JsonAdapter(PolygonFill.Parser.class)
@Builder
final class PolygonFill implements PolygonMapper {
    @NonNull
    protected final DrawFunction draw;
    @NonNull
    protected final DValue layer;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull MultiPolygon projectedGeometry) {
        return Collections.singletonList(new FillPolygon(id, this.layer.apply(tags), this.draw, projectedGeometry));
    }

    static final class Parser extends JsonParser<PolygonFill> {
        @Override
        public PolygonFill read(JsonReader in) throws IOException {
            PolygonFillBuilder builder = builder();

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
                    default:
                        throw new IllegalStateException("invalid property: " + name);
                }
            }
            in.endObject();

            return builder.build();
        }
    }
}
