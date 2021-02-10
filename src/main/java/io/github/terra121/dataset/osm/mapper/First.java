package io.github.terra121.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.dataset.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.geojson.geometry.MultiPolygon;
import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.dataset.osm.JsonParser;
import io.github.terra121.dataset.osm.OSMMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.daporkchop.lib.common.util.GenericMatcher;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Returns the result of the first of a number of mappers that returned a non-{@code null} value.
 *
 * @author DaPorkchop_
 */
@AllArgsConstructor
abstract class First<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @NonNull
    protected final M[] children;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        for (M child : this.children) {
            Collection<VectorGeometry> result = child.apply(id, tags, originalGeometry, projectedGeometry);
            if (result != null) {
                return result;
            }
        }

        return null; //none matched!
    }

    static abstract class Parser<G extends Geometry, M extends OSMMapper<G>> extends JsonParser<M> {
        protected final Class<M> mapperClass = GenericMatcher.uncheckedFind(this.getClass(), Parser.class, "M");

        @Override
        public M read(JsonReader in) throws IOException {
            List<M> children = readTypedList(in, this.mapperClass);
            checkState(!children.isEmpty(), "at least one member required!");
            return this.construct(children);
        }

        protected abstract M construct(@NonNull List<M> children);
    }

    @JsonAdapter(Line.Parser.class)
    static class Line extends First<MultiLineString, LineMapper> implements LineMapper {
        public Line(LineMapper[] children) {
            super(children);
        }

        static class Parser extends First.Parser<MultiLineString, LineMapper> {
            @Override
            protected LineMapper construct(@NonNull List<LineMapper> children) {
                return children.size() == 1 ? children.get(0) : new Line(children.toArray(new LineMapper[0]));
            }
        }
    }

    @JsonAdapter(Polygon.Parser.class)
    static class Polygon extends First<MultiPolygon, PolygonMapper> implements PolygonMapper {
        public Polygon(PolygonMapper[] children) {
            super(children);
        }

        static class Parser extends First.Parser<MultiPolygon, PolygonMapper> {
            @Override
            protected PolygonMapper construct(@NonNull List<PolygonMapper> children) {
                return children.size() == 1 ? children.get(0) : new Polygon(children.toArray(new PolygonMapper[0]));
            }
        }
    }
}
