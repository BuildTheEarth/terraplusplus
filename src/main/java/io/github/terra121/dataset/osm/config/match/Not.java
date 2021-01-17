package io.github.terra121.dataset.osm.config.match;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import io.github.terra121.dataset.osm.geojson.Geometry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

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

    static class Parser extends MatchParser {
        @Override
        public MatchCondition read(JsonReader in) throws IOException {
            return new Not(super.read(in));
        }
    }
}
