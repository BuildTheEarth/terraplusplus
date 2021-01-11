package io.github.terra121.dataset.osm.config.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.dataset.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.geojson.geometry.MultiPolygon;
import io.github.terra121.dataset.osm.Generatable;
import io.github.terra121.dataset.osm.config.JsonParser;
import io.github.terra121.dataset.osm.config.OSMMapper;
import lombok.NonNull;
import net.daporkchop.lib.common.util.GenericMatcher;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Returns a non-null, empty list.
 *
 * @author DaPorkchop_
 */
abstract class Nothing<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @Override
    public Collection<Generatable> apply(String id, @NonNull Map<String, String> tags, @NonNull G geometry) {
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
