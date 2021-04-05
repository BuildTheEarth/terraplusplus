package net.buildtheearth.terraplusplus.dataset.osm.match;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;

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
