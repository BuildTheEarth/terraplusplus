package io.github.terra121.dataset.osm.config.match;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
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
    public boolean test(String id, @NonNull Map<String, String> tags) {
        return !this.delegate.test(id, tags);
    }

    static class Parser extends MatchParser {
        @Override
        public MatchCondition read(JsonReader in) throws IOException {
            return new Not(super.read(in));
        }
    }
}
