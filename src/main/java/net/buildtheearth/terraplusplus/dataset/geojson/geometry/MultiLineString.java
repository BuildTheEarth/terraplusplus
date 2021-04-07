package net.buildtheearth.terraplusplus.dataset.geojson.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Iterators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.ProjectionFunction;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author DaPorkchop_
 */
@Getter
@ToString
@EqualsAndHashCode
@JsonDeserialize
@JsonTypeName("MultiLineString")
public final class MultiLineString implements Geometry, Iterable<LineString> {
    @Getter(onMethod_ = {
            @JsonGetter("coordinates"),
            @JsonSerialize(using = LineString.ArraySerializer.class)
    })
    protected final LineString[] lines;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MultiLineString(
            @JsonProperty(value = "coordinates", required = true) @JsonDeserialize(using = LineString.ArrayDeserializer.class) @NonNull LineString[] lines) {
        this.lines = lines;
    }

    @Override
    public Iterator<LineString> iterator() {
        return Iterators.forArray(this.lines);
    }

    @Override
    public MultiLineString project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        LineString[] out = this.lines.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new MultiLineString(out);
    }

    @Override
    public Bounds2d bounds() {
        return Arrays.stream(this.lines).map(LineString::bounds).reduce(Bounds2d::union).orElse(null);
    }
}
