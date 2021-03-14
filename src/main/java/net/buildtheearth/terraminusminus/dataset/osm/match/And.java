package net.buildtheearth.terraminusminus.dataset.osm.match;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraminusminus.dataset.geojson.Geometry;

import java.io.IOException;
import java.util.Map;

/**
 * Combines the results of multiple match conditions using a logical AND operation.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(And.Parser.class)
@RequiredArgsConstructor
final class And implements MatchCondition {
    @NonNull
    protected final MatchCondition[] delegates;

    @Override
    public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        for (MatchCondition delegate : this.delegates) {
            if (!delegate.test(id, tags, originalGeometry, projectedGeometry)) {
                return false;
            }
        }
        return true;
    }

    static class Parser extends MatchParser {
        @Override
        public MatchCondition read(JsonReader in) throws IOException {
            return new And(readTypedList(in, this).toArray(new MatchCondition[0]));
        }
    }
}
