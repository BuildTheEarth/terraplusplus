package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
public class PolygonParser extends JsonParser.Typed<PolygonMapper> {
    public static final Map<String, Class<? extends PolygonMapper>> TYPES = new Object2ObjectOpenHashMap<>();

    static {
        TYPES.put("all", All.Polygon.class);
        TYPES.put("any", Any.Polygon.class);
        TYPES.put("condition", Condition.Polygon.class);
        TYPES.put("convert", PolygonConvert.class);
        TYPES.put("first", First.Polygon.class);
        TYPES.put("nothing", Nothing.Polygon.class);

        TYPES.put("distance", PolygonDistance.class);
        TYPES.put("fill", PolygonFill.class);
    }

    public PolygonParser() {
        super("polygon", TYPES);
    }
}
