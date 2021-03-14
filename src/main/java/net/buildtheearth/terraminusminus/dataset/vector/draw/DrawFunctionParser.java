package net.buildtheearth.terraminusminus.dataset.vector.draw;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.buildtheearth.terraminusminus.dataset.osm.JsonParser;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
public class DrawFunctionParser extends JsonParser.Typed<DrawFunction> {
    public static final Map<String, Class<? extends DrawFunction>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("all", All.class);

        TYPES.put("weight_add", WeightAdd.class);
        TYPES.put("weight_clamp", WeightClamp.class);
        TYPES.put("weight_greater_than", WeightGreaterThan.class);
        TYPES.put("weight_less_than", WeightLessThan.class);

        TYPES.put("block", Block.class);
        TYPES.put("no_trees", NoTrees.class);
        TYPES.put("ocean", Ocean.class);
        TYPES.put("water", Water.class);
    }

    public DrawFunctionParser() {
        super("draw", TYPES);
    }
}
