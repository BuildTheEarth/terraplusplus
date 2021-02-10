package io.github.terra121.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.dataset.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.geojson.geometry.MultiPolygon;
import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.dataset.osm.JsonParser;
import io.github.terra121.dataset.osm.OSMMapper;
import lombok.NonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Returns a non-null, empty list.
 *
 * @author DaPorkchop_
 */
abstract class Nothing<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        return Collections.emptyList();
    }

    static abstract class Parser<G extends Geometry, M extends OSMMapper<G>> extends JsonParser<M> {
        @Override
        public M read(JsonReader in) throws IOException {
            in.beginObject();
            in.endObject();
            return this.construct();
        }

        protected abstract M construct();
    }

    @JsonAdapter(Line.Parser.class)
    static class Line extends Nothing<MultiLineString, LineMapper> implements LineMapper {
        static class Parser extends Nothing.Parser<MultiLineString, LineMapper> {
            @Override
            protected LineMapper construct() {
                return new Line();
            }
        }
    }

    @JsonAdapter(Polygon.Parser.class)
    static class Polygon extends Nothing<MultiPolygon, PolygonMapper> implements PolygonMapper {
        static class Parser extends Nothing.Parser<MultiPolygon, PolygonMapper> {
            @Override
            protected PolygonMapper construct() {
                return new Polygon();
            }
        }
    }
}
