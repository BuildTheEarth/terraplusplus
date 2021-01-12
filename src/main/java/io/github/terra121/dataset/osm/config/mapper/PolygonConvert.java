package io.github.terra121.dataset.osm.config.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.geojson.geometry.LineString;
import io.github.terra121.dataset.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.geojson.geometry.MultiPolygon;
import io.github.terra121.dataset.geojson.geometry.Polygon;
import io.github.terra121.dataset.osm.Element;
import io.github.terra121.dataset.osm.config.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.github.terra121.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(PolygonConvert.Parser.class)
interface PolygonConvert extends PolygonMapper {
    class Parser extends JsonParser.Typed<PolygonConvert> {
        static final Map<String, Class<? extends PolygonConvert>> TYPES = new Object2ObjectOpenHashMap<>();

        static {
            TYPES.put("line", Line.class);
        }

        public Parser() {
            super("convert", TYPES);
        }

        @Override
        public PolygonConvert read(JsonReader in) throws IOException {
            in.beginObject();
            PolygonConvert result = super.read(in);
            in.endObject();
            return result;
        }
    }

    @JsonAdapter(Line.Parser.class)
    @RequiredArgsConstructor
    final class Line implements PolygonConvert {
        @NonNull
        protected final LineMapper next;

        @Override
        public Collection<Element> apply(String id, @NonNull Map<String, String> tags, @NonNull MultiPolygon geometry) {
            //convert multipolygon to multilinestring
            List<LineString> lines = new ArrayList<>();
            for (Polygon polygon : geometry.polygons()) {
                lines.add(polygon.outerRing());
                lines.addAll(Arrays.asList(polygon.innerRings()));
            }

            return this.next.apply(id, tags, new MultiLineString(lines.toArray(new LineString[0])));
        }

        static class Parser extends JsonParser<Line> {
            @Override
            public Line read(JsonReader in) throws IOException {
                in.beginObject();
                LineMapper next = GSON.fromJson(in, LineMapper.class);
                in.endObject();
                return new Line(next);
            }
        }
    }
}
