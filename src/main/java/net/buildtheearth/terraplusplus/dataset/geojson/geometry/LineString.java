package net.buildtheearth.terraplusplus.dataset.geojson.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.ProjectionFunction;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Getter
@ToString
@EqualsAndHashCode
@JsonDeserialize
@JsonTypeName("LineString")
public final class LineString implements Geometry {
    @Getter(onMethod_ = {
            @JsonGetter("coordinates"),
            @JsonSerialize(using = Point.ArraySerializer.class)
    })
    protected final Point[] points;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public LineString(
            @JsonProperty(value = "coordinates", required = true) @JsonDeserialize(using = Point.ArrayDeserializer.class) @NonNull Point[] points) {
        checkArg(points.length >= 2, "LineString must contain at least 2 points!");
        this.points = points;
    }

    @JsonIgnore
    public boolean isLinearRing() {
        return this.points.length >= 4 && Objects.equals(this.points[0], this.points[this.points.length - 1]);
    }

    @Override
    public LineString project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        Point[] out = this.points.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new LineString(out);
    }

    @Override
    public Bounds2d bounds() {
        if (this.points.length == 0) {
            return null;
        }

        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        for (Point point : this.points) {
            minLon = min(minLon, point.lon);
            maxLon = max(maxLon, point.lon);
            minLat = min(minLat, point.lat);
            maxLat = max(maxLat, point.lat);
        }
        return Bounds2d.of(minLon, maxLon, minLat, maxLat);
    }

    protected static final class ArrayDeserializer extends JsonDeserializer<LineString[]> {
        public static final ArrayDeserializer INSTANCE = new ArrayDeserializer();

        @Override
        public LineString[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            checkState(p.isExpectedStartArrayToken(), "expected array start");

            List<LineString> list = new ArrayList<>();

            JsonToken token = p.nextToken();
            do {
                list.add(new LineString(Point.ArrayDeserializer.INSTANCE.deserialize(p, ctxt)));
            } while ((token = p.nextToken()) == JsonToken.START_ARRAY);
            checkState(token == JsonToken.END_ARRAY, "expected array end, but found %s", token);

            return list.toArray(new LineString[0]);
        }
    }

    protected static final class ArraySerializer extends JsonSerializer<LineString[]> {
        public static final ArraySerializer INSTANCE = new ArraySerializer();

        @Override
        public void serialize(LineString[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            for (LineString lineString : value) {
                Point.ArraySerializer.INSTANCE.serialize(lineString.points(), gen, serializers);
            }
            gen.writeEndArray();
        }
    }
}
