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
@JsonTypeName("MultiPolygon")
public final class MultiPolygon implements Geometry, Iterable<Polygon> {
    @Getter(onMethod_ = {
            @JsonGetter("coordinates"),
            @JsonSerialize(using = Polygon.ArraySerializer.class)
    })
    protected final Polygon[] polygons;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MultiPolygon(
            @JsonProperty(value = "coordinates", required = true) @JsonDeserialize(using = Polygon.ArrayDeserializer.class) @NonNull Polygon[] polygons) {
        this.polygons = polygons;
    }

    @Override
    public Iterator<Polygon> iterator() {
        return Iterators.forArray(this.polygons);
    }

    @Override
    public MultiPolygon project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        Polygon[] out = this.polygons.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new MultiPolygon(out);
    }

    @Override
    public Bounds2d bounds() {
        return Arrays.stream(this.polygons).map(Polygon::bounds).reduce(Bounds2d::union).orElse(null);
    }
}
