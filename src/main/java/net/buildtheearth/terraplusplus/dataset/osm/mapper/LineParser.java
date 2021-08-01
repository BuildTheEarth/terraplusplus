package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;

/**
 * @author DaPorkchop_
 */
public class LineParser extends JsonParser.Typed<LineMapper> {
    public static final Map<String, Class<? extends LineMapper>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("all", All.Line.class);
        TYPES.put("any", Any.Line.class);
        TYPES.put("condition", Condition.Line.class);
        TYPES.put("first", First.Line.class);
        TYPES.put("nothing", Nothing.Line.class);

        TYPES.put("narrow", LineNarrow.class);
        TYPES.put("sharp", LineSharp.class);
        TYPES.put("wide", LineWide.class);
    }

    public LineParser() {
        super("line", TYPES);
    }
}
