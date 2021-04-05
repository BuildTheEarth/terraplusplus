package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchCondition;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.daporkchop.lib.common.util.GenericMatcher;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Forwards elements to another mapper if a given {@link MatchCondition} matches.
 *
 * @author DaPorkchop_
 */
@AllArgsConstructor
abstract class Condition<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @NonNull
    protected final MatchCondition match;
    @NonNull
    protected final M emit;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        if (!this.match.test(id, tags, originalGeometry, projectedGeometry)) { //element doesn't match, emit nothing
            return null;
        }

        return this.emit.apply(id, tags, originalGeometry, projectedGeometry);
    }

    static abstract class Parser<G extends Geometry, M extends OSMMapper<G>, I extends Condition<?, ?>> extends JsonParser<I> {
        protected final Class<M> mapperClass = GenericMatcher.uncheckedFind(this.getClass(), Parser.class, "M");

        @Override
        public I read(JsonReader in) throws IOException {
            MatchCondition match = null;
            M emit = null;

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "match":
                        in.beginObject();
                        match = TerraConstants.GSON.fromJson(in, MatchCondition.class);
                        in.endObject();
                        break;
                    case "emit":
                        in.beginObject();
                        emit = TerraConstants.GSON.fromJson(in, this.mapperClass);
                        in.endObject();
                        break;
                    default:
                        throw new IllegalStateException("invalid property: " + name);
                }
            }
            in.endObject();
            return this.construct(match, emit);
        }

        protected abstract I construct(@NonNull MatchCondition match, @NonNull M emit);
    }

    @JsonAdapter(Line.Parser.class)
    static class Line extends Condition<MultiLineString, LineMapper> implements LineMapper {
        public Line(MatchCondition match, LineMapper emit) {
            super(match, emit);
        }

        static class Parser extends Condition.Parser<MultiLineString, LineMapper, Line> {
            @Override
            protected Line construct(@NonNull MatchCondition match, @NonNull LineMapper emit) {
                return new Line(match, emit);
            }
        }
    }

    @JsonAdapter(Polygon.Parser.class)
    static class Polygon extends Condition<MultiPolygon, PolygonMapper> implements PolygonMapper {
        public Polygon(MatchCondition match, PolygonMapper emit) {
            super(match, emit);
        }

        static class Parser extends Condition.Parser<MultiPolygon, PolygonMapper, Polygon> {
            @Override
            protected Polygon construct(@NonNull MatchCondition match, @NonNull PolygonMapper emit) {
                return new Polygon(match, emit);
            }
        }
    }
}
