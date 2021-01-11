package io.github.terra121.dataset.osm.config.mapper;

import io.github.terra121.dataset.osm.config.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
final class LineParser extends JsonParser.Typed<LineMapper> {
    private static final Map<String, Class<? extends LineMapper>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("all", All.Line.class);
        TYPES.put("any", Any.Line.class);
        TYPES.put("condition", Condition.Line.class);
        TYPES.put("first", First.Line.class);
        TYPES.put("nothing", Nothing.Line.class);

        TYPES.put("narrow", LineNarrow.class);
        TYPES.put("wide", LineWide.class);
    }

    public LineParser() {
        super("line", TYPES);
    }
}
