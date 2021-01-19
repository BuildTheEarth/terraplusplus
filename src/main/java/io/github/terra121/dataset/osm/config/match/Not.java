package io.github.terra121.dataset.osm.config.match;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.osm.config.JsonParser;
import io.github.terra121.dataset.osm.geojson.Geometry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

import static io.github.terra121.TerraConstants.*;

/**
 * Inverts the result of a single match condition.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Not.Parser.class)
@RequiredArgsConstructor
final class Not implements MatchCondition {
    @NonNull
    protected final MatchCondition delegate;

    @Override
    public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        return !this.delegate.test(id, tags, originalGeometry, projectedGeometry);
    }

    static class Parser extends JsonParser<Not> {
        @Override
        public Not read(JsonReader in) throws IOException {
            in.beginObject();
            MatchCondition delegate = GSON.fromJson(in, MatchCondition.class);
            in.endObject();
            return new Not(delegate);
        }
    }
}
