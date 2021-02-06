package io.github.terra121.dataset.vector.draw;

import io.github.terra121.dataset.osm.config.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
public class DrawFunctionParser extends JsonParser.Typed<DrawFunction> {
    public static final Map<String, Class<? extends DrawFunction>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("add", Add.class);
        TYPES.put("clamp", Clamp.class);

        TYPES.put("block", BlockDraw.class);
        TYPES.put("ocean", Ocean.class);
        TYPES.put("water", Water.class);
    }

    public DrawFunctionParser() {
        super("draw", TYPES);
    }
}
