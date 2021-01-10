package io.github.terra121.dataset.osm.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(MatchCondition.Adapter.class)
@FunctionalInterface
public interface MatchCondition {
    boolean test(@NonNull Map<String, String> tags);

    final class Adapter extends BetterTypeAdapter<MatchCondition> {
        @Override
        public void write(JsonWriter out, MatchCondition value) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public MatchCondition read(JsonReader in) throws IOException {
            in.beginObject();

            String name = in.nextName();
            MatchCondition condition;
            switch (name) {
                case "tag": {
                    in.beginObject();
                    String key = in.nextName();
                    if (in.peek() == JsonToken.NULL) { //only check if key is set, ignore value
                        condition = tags -> tags.containsKey(key);
                    } else if (in.peek() == JsonToken.BEGIN_ARRAY) { //check if key is set to any one of the given values
                        Set<String> expectedValues = ImmutableSet.copyOf(readList(in, JsonReader::nextString));
                        condition = tags -> expectedValues.contains(tags.get(key));
                    } else { //check if key is set to exactly the given value
                        String expectedValue = in.nextString();
                        condition = tags -> expectedValue.equals(tags.get(key));
                    }

                    in.endObject();
                    break;
                }
                case "and": {
                    MatchCondition[] conditions = readList(in, this::read).toArray(new MatchCondition[0]);
                    condition = tags -> {
                        for (MatchCondition c : conditions) {
                            if (!c.test(tags)) {
                                return false;
                            }
                        }
                        return true;
                    };
                    break;
                }
                case "or": {
                    MatchCondition[] conditions = readList(in, this::read).toArray(new MatchCondition[0]);
                    condition = tags -> {
                        for (MatchCondition c : conditions) {
                            if (c.test(tags)) {
                                return true;
                            }
                        }
                        return false;
                    };
                    break;
                }
                case "not": {
                    MatchCondition c = this.read(in);
                    condition = tags -> !c.test(tags);
                    break;
                }
                default:
                    throw new IllegalArgumentException("unknown match condition: " + name);
            }
            in.endObject();

            return condition;
        }
    }
}
