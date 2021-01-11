package io.github.terra121.dataset.osm.config.match;

import io.github.terra121.dataset.osm.config.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
class MatchParser extends JsonParser.Typed<MatchCondition> {
    private static final Map<String, Class<? extends MatchCondition>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("and", And.class);
        TYPES.put("id", Id.class);
        TYPES.put("not", Not.class);
        TYPES.put("or", Or.class);
        TYPES.put("tag", Tag.class);
    }

    public MatchParser() {
        super("match", TYPES);
    }
}
