package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.Builder;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.dataset.osm.dvalue.DValue;
import net.buildtheearth.terraplusplus.dataset.vector.draw.DrawFunction;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.polygon.DistancePolygon;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(PolygonDistance.Parser.class)
@Builder
final class PolygonDistance implements PolygonMapper {
    @NonNull
    protected final DrawFunction draw;
    @NonNull
    protected final DValue layer;
    @Builder.Default
    protected final int maxDist = 2;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull MultiPolygon projectedGeometry) {
        return Collections.singletonList(new DistancePolygon(id, this.layer.apply(tags), this.draw, projectedGeometry, this.maxDist));
    }

    static final class Parser extends JsonParser<PolygonDistance> {
        @Override
        public PolygonDistance read(JsonReader in) throws IOException {
            PolygonDistanceBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "draw":
                        in.beginObject();
                        builder.draw(TerraConstants.GSON.fromJson(in, DrawFunction.class));
                        in.endObject();
                        break;
                    case "layer":
                        in.beginObject();
                        builder.layer(TerraConstants.GSON.fromJson(in, DValue.class));
                        in.endObject();
                        break;
                    case "maxDist":
                        builder.maxDist(in.nextInt());
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
