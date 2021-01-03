package io.github.terra121.dataset.geojson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.terra121.dataset.geojson.geometry.Point;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
abstract class AbstractGeoJSONDeserializer<T extends GeoJSONObject> extends TypeAdapter<T> {
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
        switch (type) {
            case "Point":
                checkState("coordinates".equals(in.nextName()));
                return this.readPoint(in);
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
}
