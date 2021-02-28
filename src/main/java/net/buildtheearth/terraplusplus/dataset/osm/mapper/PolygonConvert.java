package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.LineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.Polygon;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(PolygonConvert.Parser.class)
public interface PolygonConvert extends PolygonMapper {
    class Parser extends JsonParser.Typed<PolygonConvert> {
        public static final Map<String, Class<? extends PolygonConvert>> TYPES = new Object2ObjectOpenHashMap<>();

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
        public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull MultiPolygon projectedGeometry) {
            //convert multipolygon to multilinestring
            List<LineString> lines = new ArrayList<>();
            for (Polygon polygon : projectedGeometry.polygons()) {
                lines.add(polygon.outerRing());
                lines.addAll(Arrays.asList(polygon.innerRings()));
            }

            return this.next.apply(id, tags, originalGeometry, new MultiLineString(lines.toArray(new LineString[0])));
        }

        static class Parser extends JsonParser<Line> {
            @Override
            public Line read(JsonReader in) throws IOException {
                in.beginObject();
                LineMapper next = TerraConstants.GSON.fromJson(in, LineMapper.class);
                in.endObject();
                return new Line(next);
            }
        }
    }
}
