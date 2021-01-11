package io.github.terra121.dataset.osm.config.match;

import com.google.gson.annotations.JsonAdapter;
import lombok.NonNull;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(MatchParser.class)
@FunctionalInterface
public interface MatchCondition {
    /**
     * Always returns {@code false}.
     */
    MatchCondition FALSE = (id, tags) -> false;

    boolean test(String id, @NonNull Map<String, String> tags);
}
