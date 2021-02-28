package net.buildtheearth.terraplusplus.dataset.osm.dvalue;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
public class DValueParser extends JsonParser.Typed<DValue> {
    public static final Map<String, Class<? extends DValue>> TYPES = new Object2ObjectOpenHashMap<>();

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
