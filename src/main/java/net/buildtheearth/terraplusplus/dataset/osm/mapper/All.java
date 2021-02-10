package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.daporkchop.lib.common.util.GenericMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Returns the combined results of all of a number of mappers, or {@code null} if any one of them returns {@code null}.
 *
 * @author DaPorkchop_
 */
@AllArgsConstructor
abstract class All<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @NonNull
    protected final M[] children;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        List<VectorGeometry> out = new ArrayList<>();
        int i = 0;
        for (M child : this.children) {
            Collection<VectorGeometry> result = child.apply(id + '/' + i++, tags, originalGeometry, projectedGeometry);
            if (result == null) { //don't bother processing further children
                return null;
            }
            out.addAll(result);
        }

        return out;
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
    static class Line extends All<MultiLineString, LineMapper> implements LineMapper {
        public Line(LineMapper[] children) {
            super(children);
        }

        static class Parser extends All.Parser<MultiLineString, LineMapper> {
            @Override
            protected LineMapper construct(@NonNull List<LineMapper> children) {
                return children.size() == 1 ? children.get(0) : new Line(children.toArray(new LineMapper[0]));
            }
        }
    }

    @JsonAdapter(Polygon.Parser.class)
    static class Polygon extends All<MultiPolygon, PolygonMapper> implements PolygonMapper {
        public Polygon(PolygonMapper[] children) {
            super(children);
        }

        static class Parser extends All.Parser<MultiPolygon, PolygonMapper> {
            @Override
            protected PolygonMapper construct(@NonNull List<PolygonMapper> children) {
                return children.size() == 1 ? children.get(0) : new Polygon(children.toArray(new PolygonMapper[0]));
            }
        }
    }
}
