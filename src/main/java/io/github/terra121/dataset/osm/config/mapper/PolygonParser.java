package io.github.terra121.dataset.osm.config.mapper;

import io.github.terra121.dataset.osm.config.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
class PolygonParser extends JsonParser.Typed<PolygonMapper> {
    private static final Map<String, Class<? extends PolygonMapper>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("all", All.Polygon.class);
        TYPES.put("any", Any.Polygon.class);
        TYPES.put("condition", Condition.Polygon.class);
        TYPES.put("convert", PolygonConvert.class);
        TYPES.put("first", First.Polygon.class);
        TYPES.put("nothing", Nothing.Polygon.class);
    }

    public PolygonParser() {
        super("polygon", TYPES);
    }
}
