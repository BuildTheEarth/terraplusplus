package io.github.terra121.dataset.osm.draw;

import io.github.terra121.dataset.osm.config.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
class DrawFunctionParser extends JsonParser.Typed<DrawFunction> {
    protected static final Map<String, Class<? extends DrawFunction>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("block", Block.class);
        TYPES.put("water", Water.class);
    }

    public DrawFunctionParser() {
        super("draw", TYPES);
    }
}
