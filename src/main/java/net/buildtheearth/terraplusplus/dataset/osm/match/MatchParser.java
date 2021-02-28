package net.buildtheearth.terraplusplus.dataset.osm.match;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;

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
