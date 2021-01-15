/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2020-2021 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package io.github.terra121.dataset.osm.geojson;

import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import io.github.terra121.dataset.osm.geojson.object.Feature;
import io.github.terra121.dataset.osm.geojson.object.FeatureCollection;
import io.github.terra121.dataset.osm.geojson.object.Reference;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
final class ObjectDeserializer extends AbstractGeoJSONDeserializer<GeoJSONObject> {
    @Getter
    private final GeometryDeserializer geometryDeserializer = new GeometryDeserializer();

    public ObjectDeserializer() {
        super("object");
    }

    @Override
    protected GeoJSONObject read0(String type, JsonReader in) throws IOException {
        switch (type) {
            case "Feature":
                return this.readFeature(in);
            case "FeatureCollection":
                return this.readFeatureCollection(in);
            case "Reference":
                return this.readReference(in);
        }

        return super.readGeometry(type, in);
    }

    protected Feature readFeature(JsonReader in) throws IOException {
        Geometry geometry = null;
        Map<String, String> properties = null;
        String id = null;

        while (in.peek() == JsonToken.NAME) {
            switch (in.nextName()) {
                case "geometry":
                    geometry = this.geometryDeserializer.read(in);
                    break;
                case "properties":
                    if (in.peek() != JsonToken.NULL) {
                        in.beginObject();
                        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
                        while (in.peek() != JsonToken.END_OBJECT) {
                            builder.put(in.nextName(), in.nextString());
                        }
                        in.endObject();
                        properties = builder.build();
                    } else {
                        in.nextNull();
                    }
                    break;
                case "id":
                    id = in.nextString();
                    break;
                default:
                    throw new IllegalArgumentException("invalid field name ");
            }
        }

        return new Feature(geometry, properties, id);
    }

    protected FeatureCollection readFeatureCollection(JsonReader in) throws IOException {
        String fieldName = in.nextName();
        checkState("features".equals(fieldName), "unexpected field \"%s\" in FeatureCollection object", fieldName);

        List<Feature> features = new ArrayList<>();
        in.beginArray();
        while (in.peek() != JsonToken.END_ARRAY) {
            in.beginObject();

            checkState("type".equals(in.nextName()), "invalid FeatureCollection: doesn't start with type!");
            String type = in.nextString();
            checkState("Feature".equals(type), "FeatureCollection contains non-Feature element \"%s\"", type);
            features.add(this.readFeature(in));
            in.endObject();
        }
        in.endArray();

        return new FeatureCollection(features.toArray(new Feature[0]));
    }

    protected Reference readReference(JsonReader in) throws IOException {
        String fieldName = in.nextName();
        checkState("location".equals(fieldName), "unexpected field \"%s\" in Reference object", fieldName);

        return new Reference(in.nextString());
    }
}
