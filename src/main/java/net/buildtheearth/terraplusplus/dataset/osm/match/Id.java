package net.buildtheearth.terraplusplus.dataset.osm.match;

import com.google.common.collect.ImmutableSet;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Matches values based on their OpenStreetMap ID.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Id.Parser.class)
@FunctionalInterface
interface Id extends MatchCondition {
    /**
     * Matches any ID contained in a {@link Set}.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    final class Any implements Id {
        @NonNull
        protected final Set<String> expectedIds;

        @Override
        public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
            return this.expectedIds.contains(id);
        }
    }

    /**
     * Matches a single ID.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    final class Exactly implements Id {
        protected final String expectedId;

        @Override
        public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
            return Objects.equals(this.expectedId, id);
        }
    }

    class Parser extends JsonParser<MatchCondition> {
        @Override
        public MatchCondition read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.BEGIN_ARRAY) { //check if key is set to any one of the given values
                Set<String> expectedIds = ImmutableSet.copyOf(readList(in, reader -> reader.nextString().intern()));
                if (expectedIds.isEmpty()) { //if no IDs are given, no ID can possibly match
                    return FALSE;
                } else if (expectedIds.size() == 1) { //if only a single ID is given there's no need to compare against a set
                    return new Exactly(expectedIds.iterator().next());
                } else {
                    return new Any(expectedIds);
                }
            } else { //check if key is set to exactly the given value
                return new Exactly(in.nextString().intern());
            }
        }
    }
}
