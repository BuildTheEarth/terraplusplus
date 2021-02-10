package net.buildtheearth.terraplusplus.dataset.osm.match;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;

/**
 * Combines the results of multiple match conditions using a logical AND operation.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Intersects.Parser.class)
@Getter
@Builder
final class Intersects implements MatchCondition, Bounds2d {
    protected final double minX;
    protected final double maxX;
    protected final double minZ;
    protected final double maxZ;

    @Override
    public boolean test(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        Bounds2d bounds = originalGeometry.bounds();
        return bounds != null && this.intersects(bounds);
    }

    static class Parser extends JsonParser<Intersects> {
        @Override
        public Intersects read(JsonReader in) throws IOException {
            IntersectsBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "minX":
                        builder.minX(in.nextDouble());
                        break;
                    case "maxX":
                        builder.maxX(in.nextDouble());
                        break;
                    case "minZ":
                        builder.minZ(in.nextDouble());
                        break;
                    case "maxZ":
                        builder.maxZ(in.nextDouble());
                        break;
                    default:
                        throw new IllegalStateException("invalid property: " + name);
                }
            }
            in.endObject();

            return builder.build();
        }
    }
}
