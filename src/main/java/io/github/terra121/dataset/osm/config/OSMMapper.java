package io.github.terra121.dataset.osm.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.geojson.Geometry;
import io.github.terra121.dataset.geojson.geometry.LineString;
import io.github.terra121.dataset.geojson.geometry.Polygon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.terra121.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@Getter
@Builder
public final class OSMMapper<G extends Geometry> {
    @NonNull
    protected final MatchCondition match;
    @NonNull
    protected final OSMEmitter<G>[] emit;

    @AllArgsConstructor
    static abstract class Adapter<G extends Geometry> extends BetterTypeAdapter<OSMMapper<G>> {
        @NonNull
        protected final String name;
        @NonNull
        protected final Map<String, Class<? extends OSMEmitter<G>>> emitterTypes;

        @Override
        public OSMMapper<G> read(JsonReader in) throws IOException {
            OSMMapperBuilder<G> builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "match":
                        builder.match(GSON.fromJson(in, MatchCondition.class));
                        break;
                    case "emit": {
                        List<OSMEmitter<G>> emitters = new ArrayList<>();

                        if (in.peek() != JsonToken.NULL) {
                            in.beginObject();
                            while (in.peek() != JsonToken.END_OBJECT) {
                                String emitterName = in.nextName();
                                Class<? extends OSMEmitter<G>> emitterClass = this.emitterTypes.get(emitterName);
                                checkArg(emitterClass != null, "emitter \"%s\" is not known by geometry type \"%s\"!", emitterName, this.name);
                                emitters.add(GSON.fromJson(in, emitterClass));
                            }
                            in.endObject();
                        }

                        builder.emit(uncheckedCast(emitters.toArray(new OSMEmitter[0])));
                        break;
                    }
                    default:
                        throw new IllegalStateException("invalid property: " + name);
                }
            }
            in.endObject();

            return builder.build();
        }
    }

    static final class LineAdapter extends Adapter<LineString> {
        public LineAdapter() {
            super("line", ImmutableMap.<String, Class<? extends OSMEmitter<LineString>>>builder()
                    .put("narrow", OSMEmitter.NarrowLine.class)
                    .put("wide", OSMEmitter.WideLine.class)
                    .build());
        }
    }

    static final class PolygonAdapter extends Adapter<Polygon> {
        public PolygonAdapter() {
            super("polygon", ImmutableMap.<String, Class<? extends OSMEmitter<Polygon>>>builder()
                    .build());
        }
    }
}
