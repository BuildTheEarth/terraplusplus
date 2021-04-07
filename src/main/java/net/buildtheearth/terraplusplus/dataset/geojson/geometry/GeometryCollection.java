package net.buildtheearth.terraplusplus.dataset.geojson.geometry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
@Getter(onMethod_ = { @JsonGetter })
@ToString
@EqualsAndHashCode
@JsonDeserialize
@JsonTypeName("GeometryCollection")
public final class GeometryCollection implements Geometry, Iterable<Geometry> {
    protected final Geometry[] geometries;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public GeometryCollection(
            @JsonProperty(value = "geometries", required = true) @NonNull Geometry[] geometries) {
        this.geometries = geometries;
    }

    @Override
    public Iterator<Geometry> iterator() {
        return Iterators.forArray(this.geometries);
    }

    @Override
    public Geometry project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        Geometry[] out = this.geometries.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new GeometryCollection(out);
    }

    @Override
    public Bounds2d bounds() {
        return Arrays.stream(this.geometries).map(Geometry::bounds).reduce(Bounds2d::union).orElse(null);
    }
}
