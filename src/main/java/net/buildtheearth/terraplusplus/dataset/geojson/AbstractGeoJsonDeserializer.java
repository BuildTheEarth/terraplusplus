package net.buildtheearth.terraplusplus.dataset.geojson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.GeometryCollection;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.LineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPoint;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.Point;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.Polygon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
abstract class AbstractGeoJsonDeserializer<T extends GeoJsonObject> extends TypeAdapter<T> {
    @NonNull
    protected final String name;

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public T read(JsonReader in) throws IOException {
        in.beginObject();
        checkState("type".equals(in.nextName()), "invalid GeoJSON %s: doesn't start with type!", this.name);

        String type = in.nextString();
        T obj = this.read0(type, in);
        checkState(obj != null, "unknown GeoJSON %s type: \"%s\"!", this.name, type);
        in.endObject();
        return obj;
    }

    protected abstract T read0(String type, JsonReader in) throws IOException;

    protected final Geometry readGeometry(String type, JsonReader in) throws IOException {
        String fieldName = in.nextName();

        if ("GeometryCollection".equals(type)) { //special handling for GeometryCollection
            checkState("geometries".equals(fieldName), "unexpected field \"%s\" in GeometryCollection object", fieldName);
            List<Geometry> geometries = new ArrayList<>();
            in.beginArray();
            while (in.peek() != JsonToken.END_ARRAY) {
                geometries.add(this.geometryDeserializer().read(in));
            }
            in.endArray();
            return new GeometryCollection(geometries.toArray(new Geometry[0]));
        }

        checkState("coordinates".equals(fieldName), "unexpected field \"%s\" in %s object", fieldName, type);
        switch (type) {
            case "Point":
                return this.readPoint(in);
            case "MultiPoint":
                return new MultiPoint(this.readPoints(in));
            case "LineString":
                return this.readLineString(in);
            case "MultiLineString":
                return new MultiLineString(this.readLineStrings(in));
            case "Polygon":
                return this.readPolygon(in);
            case "MultiPolygon":
                return new MultiPolygon(this.readPolygons(in));
        }
        return null;
    }

    protected Point readPoint(JsonReader in) throws IOException {
        in.beginArray();
        Point point = new Point(in.nextDouble(), in.nextDouble());
        if (in.peek() == JsonToken.NUMBER) { //optional elevation
            in.nextDouble();
        }
        in.endArray();
        return point;
    }

    protected Point[] readPoints(JsonReader in) throws IOException {
        List<Point> points = new ArrayList<>();
        in.beginArray();
        while (in.peek() != JsonToken.END_ARRAY) {
            points.add(this.readPoint(in));
        }
        in.endArray();
        return points.toArray(new Point[0]);
    }

    protected LineString readLineString(JsonReader in) throws IOException {
        return new LineString(this.readPoints(in));
    }

    protected LineString[] readLineStrings(JsonReader in) throws IOException {
        List<LineString> lines = new ArrayList<>();
        in.beginArray();
        while (in.peek() != JsonToken.END_ARRAY) {
            lines.add(this.readLineString(in));
        }
        in.endArray();
        return lines.toArray(new LineString[0]);
    }

    protected Polygon readPolygon(JsonReader in) throws IOException {
        LineString[] lines = this.readLineStrings(in);
        return new Polygon(lines[0], Arrays.copyOfRange(lines, 1, lines.length));
    }

    protected Polygon[] readPolygons(JsonReader in) throws IOException {
        List<Polygon> polygons = new ArrayList<>();
        in.beginArray();
        while (in.peek() != JsonToken.END_ARRAY) {
            polygons.add(this.readPolygon(in));
        }
        in.endArray();
        return polygons.toArray(new Polygon[0]);
    }

    protected abstract GeometryDeserializer geometryDeserializer();
}
