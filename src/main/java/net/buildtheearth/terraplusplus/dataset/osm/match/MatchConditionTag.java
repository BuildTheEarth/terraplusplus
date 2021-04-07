package net.buildtheearth.terraplusplus.dataset.osm.match;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@JsonDeserialize(builder = MatchConditionTag.Builder.class)
public final class MatchConditionTag implements MatchCondition {
    @Getter(onMethod_ = { @JsonValue })
    @NonNull
    protected final Map<String, Set<String>> tags;

    @Override
    public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        for (Map.Entry<String, Set<String>> entry : this.tags.entrySet()) {
            String value = tags.get(entry.getKey());
            if (value == null //the tag isn't set
                || (entry.getValue() != null && entry.getValue().contains(value))) { //the tag's value isn't whitelisted
                return false;
            }
        }
        return true;
    }

    @JsonPOJOBuilder
    public static class Builder {
        protected final Map<String, Set<String>> tags = new Object2ObjectOpenHashMap<>();

        @JsonAnySetter
        private void setter(@NonNull String key, JsonNode node) {
            if (node.isNull()) {
                this.tags.put(key, null);
            } else if (node.isArray()) {
                this.tags.put(key, ImmutableSet.copyOf(StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText).map(String::intern).toArray(String[]::new)));
            } else {
                this.tags.put(key, ImmutableSet.of(node.asText()));
            }
        }

        public MatchConditionTag build() {
            return new MatchConditionTag(this.tags);
        }
    }
}
