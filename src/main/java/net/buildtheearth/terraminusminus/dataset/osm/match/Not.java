package net.buildtheearth.terraminusminus.dataset.osm.match;

import java.io.IOException;
import java.util.Map;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraminusminus.TerraConstants;
import net.buildtheearth.terraminusminus.dataset.geojson.Geometry;
import net.buildtheearth.terraminusminus.dataset.osm.JsonParser;

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
            MatchCondition delegate = TerraConstants.GSON.fromJson(in, MatchCondition.class);
            in.endObject();
            return new Not(delegate);
        }
    }
}
