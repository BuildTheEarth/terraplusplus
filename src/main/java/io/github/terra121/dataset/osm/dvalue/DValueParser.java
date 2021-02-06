package io.github.terra121.dataset.osm.dvalue;

import io.github.terra121.dataset.osm.JsonParser;
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
        TYPES.put("floor_div", BiOp.FloorDiv.class);
        TYPES.put("min", BiOp.Min.class);
        TYPES.put("max", BiOp.Max.class);
        TYPES.put("tag", Tag.class);
    }

    public DValueParser() {
        super("dvalue", TYPES);
    }
}
