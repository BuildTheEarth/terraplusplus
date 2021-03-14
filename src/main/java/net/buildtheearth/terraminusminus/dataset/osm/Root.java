package net.buildtheearth.terraminusminus.dataset.osm;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraminusminus.TerraConstants;
import net.buildtheearth.terraminusminus.dataset.geojson.Geometry;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.LineString;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.MultiPoint;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.Point;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.Polygon;
import net.buildtheearth.terraminusminus.dataset.osm.mapper.LineMapper;
import net.buildtheearth.terraminusminus.dataset.osm.mapper.PolygonMapper;
import net.buildtheearth.terraminusminus.dataset.vector.geometry.VectorGeometry;
import net.daporkchop.lib.common.util.PorkUtil;

/**
 * Root of the OpenStreetMap configuration.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(Root.Parser.class)
@Getter
@Builder
final class Root implements OSMMapper<Geometry> {
    @NonNull
    protected final LineMapper line;
    @NonNull
    protected final PolygonMapper polygon;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        if (projectedGeometry instanceof Point || projectedGeometry instanceof MultiPoint) { //points can't be generated
            return null;
        }

        //convert to multi type if not already
        if (projectedGeometry instanceof LineString) {
            projectedGeometry = new MultiLineString(new LineString[]{ (LineString) projectedGeometry });
        } else if (projectedGeometry instanceof Polygon) {
            projectedGeometry = new MultiPolygon(new Polygon[]{ (Polygon) projectedGeometry });
        }

        if (projectedGeometry instanceof MultiLineString) {
            return this.line.apply(id, tags, originalGeometry, (MultiLineString) projectedGeometry);
        } else if (projectedGeometry instanceof MultiPolygon) {
            return this.polygon.apply(id, tags, originalGeometry, (MultiPolygon) projectedGeometry);
        } else {
            throw new IllegalArgumentException("unsupported geometry type: " + PorkUtil.className(projectedGeometry));
        }
    }

    static final class Parser extends JsonParser<Root> {
        @Override
        public Root read(JsonReader in) throws IOException {
            RootBuilder builder = builder();

            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                switch (name) {
                    case "line":
                        in.beginObject();
                        builder.line(TerraConstants.GSON.fromJson(in, LineMapper.class));
                        in.endObject();
                        break;
                    case "polygon":
                        in.beginObject();
                        builder.polygon(TerraConstants.GSON.fromJson(in, PolygonMapper.class));
                        in.endObject();
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
