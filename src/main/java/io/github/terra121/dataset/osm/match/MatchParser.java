package io.github.terra121.dataset.osm.match;

import io.github.terra121.dataset.osm.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
public class MatchParser extends JsonParser.Typed<MatchCondition> {
    public static final Map<String, Class<? extends MatchCondition>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("and", And.class);
        TYPES.put("id", Id.class);
        TYPES.put("not", Not.class);
        TYPES.put("or", Or.class);

        TYPES.put("intersects", Intersects.class);
        TYPES.put("tag", Tag.class);
    }

    public MatchParser() {
        super("match", TYPES);
    }
}
