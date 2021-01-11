package io.github.terra121.dataset.osm.config.mapper;

import io.github.terra121.dataset.osm.config.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
final class PolygonParser extends JsonParser.Typed<PolygonMapper> {
    private static final Map<String, Class<? extends PolygonMapper>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put(null, Condition.Polygon.class);
        TYPES.put("condition", Condition.Polygon.class);
    }

    public PolygonParser() {
        super("polygon", TYPES);
    }
}
