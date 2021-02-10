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
import java.util.Set;

/**
 * Matches a single OpenStreetMap tag.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Tag.Parser.class)
@FunctionalInterface
interface Tag extends MatchCondition {
    /**
     * Matches tags with the given key, regardless of value.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    final class All implements Tag {
        @NonNull
        protected final String key;

        @Override
        public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
            return tags.containsKey(this.key);
        }
    }

    /**
     * Matches tags with the given key mapped to any one of the values in a {@link Set}.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    final class Any implements Tag {
        @NonNull
        protected final String key;
        @NonNull
        protected final Set<String> expectedValues;

        @Override
        public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
            return this.expectedValues.contains(tags.get(this.key));
        }
    }

    /**
     * Matches tags with the given key mapped to the given value.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    final class Exactly implements Tag {
        @NonNull
        protected final String key;
        @NonNull
        protected final String value;

        @Override
        public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
            return this.value.equals(tags.get(this.key));
        }
    }

    class Parser extends JsonParser<MatchCondition> {
        @Override
        public MatchCondition read(JsonReader in) throws IOException {
            MatchCondition result;
            in.beginObject();
            String key = in.nextName().intern();

            if (in.peek() == JsonToken.NULL) { //only check if key is set, ignore value
                in.nextNull();
                result = new All(key);
            } else if (in.peek() == JsonToken.BEGIN_ARRAY) { //check if key is set to any one of the given values
                Set<String> expectedValues = ImmutableSet.copyOf(readList(in, reader -> reader.nextString().intern()));
                if (expectedValues.isEmpty()) { //if no values are given, no tag can possibly match
                    result = FALSE;
                } else if (expectedValues.size() == 1) { //if only a single value is given there's no need to compare against a set
                    result = new Exactly(key, expectedValues.iterator().next());
                } else {
                    result = new Any(key, expectedValues);
                }
            } else { //check if key is set to exactly the given value
                result = new Exactly(key, in.nextString().intern());
            }

            in.endObject();
            return result;
        }
    }
}
