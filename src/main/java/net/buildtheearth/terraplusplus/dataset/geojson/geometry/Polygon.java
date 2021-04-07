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
import java.util.Arrays;
import java.util.List;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@Getter
@ToString
@EqualsAndHashCode
@JsonDeserialize
@JsonTypeName("Polygon")
public final class Polygon implements Geometry {
    protected final LineString outerRing;
    protected final LineString[] innerRings;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Polygon(
            @JsonProperty(value = "coordinates", required = true) @JsonDeserialize(using = LineString.ArrayDeserializer.class) @NonNull LineString[] rings) {
        checkArg(rings.length >= 1, "polygon must contain at least one ring!");
        LineString outerRing = rings[0];
        LineString[] innerRings = Arrays.copyOfRange(rings, 1, rings.length);

        checkArg(outerRing.isLinearRing(), "outerRing is not a linear ring!");
        for (int i = 0; i < innerRings.length; i++) {
            checkArg(innerRings[i].isLinearRing(), "innerRings[%d] is not a linear ring!", i);
        }
        this.outerRing = outerRing;
        this.innerRings = innerRings;
    }

    public Polygon(@NonNull LineString outerRing, @NonNull LineString[] innerRings) {
        checkArg(outerRing.isLinearRing(), "outerRing is not a linear ring!");
        for (int i = 0; i < innerRings.length; i++) {
            checkArg(innerRings[i].isLinearRing(), "innerRings[%d] is not a linear ring!", i);
        }
        this.outerRing = outerRing;
        this.innerRings = innerRings;
    }

    @JsonGetter("coordinates")
    @JsonSerialize(using = LineString.ArraySerializer.class)
    private LineString[] coordinates() {
        LineString[] merged = new LineString[this.innerRings.length + 1];
        merged[0] = this.outerRing;
        System.arraycopy(this.innerRings, 0, merged, 1, this.innerRings.length);
        return merged;
    }

    @Override
    public Polygon project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        LineString outerRing = this.outerRing.project(projection);
        LineString[] innerRings = this.innerRings.clone();
        for (int i = 0; i < innerRings.length; i++) {
            innerRings[i] = innerRings[i].project(projection);
        }
        return new Polygon(outerRing, innerRings);
    }

    @Override
    public Bounds2d bounds() {
        return this.outerRing.bounds();
    }

    protected static final class ArrayDeserializer extends JsonDeserializer<Polygon[]> {
        @Override
        public Polygon[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            checkState(p.isExpectedStartArrayToken(), "expected array start");

            List<Polygon> list = new ArrayList<>();

            JsonToken token = p.nextToken();
            do {
                list.add(new Polygon(LineString.ArrayDeserializer.INSTANCE.deserialize(p, ctxt)));
            } while ((token = p.nextToken()) == JsonToken.START_ARRAY);
            checkState(token == JsonToken.END_ARRAY, "expected array end, but found %s", token);

            return list.toArray(new Polygon[0]);
        }
    }

    protected static final class ArraySerializer extends JsonSerializer<Polygon[]> {
        @Override
        public void serialize(Polygon[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray();
            for (Polygon polygon : value) {
                LineString.ArraySerializer.INSTANCE.serialize(polygon.coordinates(), gen, serializers);
            }
            gen.writeEndArray();
        }
    }
}
