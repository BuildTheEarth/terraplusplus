package io.github.terra121.dataset.osm.config.dvalue;

import io.github.terra121.dataset.osm.config.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
class DValueParser extends JsonParser.Typed<DValue> {
    private static final Map<String, Class<? extends DValue>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("+", BiOp.Add.class);
        TYPES.put("-", BiOp.Subtract.class);
        TYPES.put("*", BiOp.Multiply.class);
        TYPES.put("/", BiOp.Divide.class);

        TYPES.put("constant", Constant.class);
        TYPES.put("tag", Tag.class);
        TYPES.put("min", BiOp.Min.class);
        TYPES.put("max", BiOp.Max.class);
    }

    public DValueParser() {
        super("dvalue", TYPES);
    }
}
