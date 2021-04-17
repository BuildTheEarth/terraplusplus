package net.buildtheearth.terraplusplus.dataset.geojson.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.ProjectionFunction;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
@JsonDeserialize
@JsonTypeName("Point")
public final class Point implements Geometry {
    protected transient final double lon;
    protected transient final double lat;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Point(
            @JsonProperty(value = "coordinates", required = true) @NonNull double[] coords) {
        checkArg(coords.length == 2 || coords.length == 3, "invalid number of point coordinates: %d", coords.length);
        this.lon = coords[0];
        this.lat = coords[1];
    }

    @JsonGetter("coordinates")
    private double[] coordinates() {
        return new double[]{ this.lon, this.lat };
    }

    @Override
    public Point project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        double[] proj = projection.project(this.lon, this.lat);
        return new Point(proj[0], proj[1]);
    }

    @Override
    public Bounds2d bounds() {
        return Bounds2d.of(this.lon, this.lon, this.lat, this.lat);
    }

    protected static final class ArrayDeserializer extends JsonDeserializer<Point[]> {
        public static final ArrayDeserializer INSTANCE = new ArrayDeserializer();

        @Override
        public Point[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            checkState(p.isExpectedStartArrayToken(), "expected array start");

            List<Point> list = new ArrayList<>();

            JsonToken token = p.nextToken();
            do {
                checkState(token == JsonToken.START_ARRAY, "expected array start, but found %s", token);
                token = p.nextToken();
                checkState(token.isNumeric(), "expected number, but found %s", token);
                double lon = p.getDoubleValue();

                token = p.nextToken();
                checkState(token.isNumeric(), "expected number, but found %s", token);
                double lat = p.getDoubleValue();

                token = p.nextToken();
                if (token != JsonToken.END_ARRAY) { //third dimension (discard it)
                    checkState(token.isNumeric(), "expected number or array end, but found %s", token);

                    token = p.nextToken();
                    checkState(token == JsonToken.END_ARRAY, "expected array end, but found %s", token);
                }

                list.add(new Point(lon, lat));
            } while ((token = p.nextToken()) == JsonToken.START_ARRAY);
            checkState(token == JsonToken.END_ARRAY, "expected array end, but found %s", token);

            return list.toArray(new Point[0]);
        }
    }

    protected static final class ArraySerializer extends JsonSerializer<Point[]> {
        public static final ArraySerializer INSTANCE = new ArraySerializer();

        @Override
        public void serialize(Point[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            for (Point point : value) {
                gen.writeStartArray();
                gen.writeNumber(point.lon);
                gen.writeNumber(point.lat);
                gen.writeEndArray();
            }
            gen.writeEndArray();
        }
    }
}
