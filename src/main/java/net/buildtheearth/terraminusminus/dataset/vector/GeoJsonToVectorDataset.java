package net.buildtheearth.terraminusminus.dataset.vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraminusminus.dataset.geojson.GeoJsonObject;
import net.buildtheearth.terraminusminus.dataset.geojson.Geometry;
import net.buildtheearth.terraminusminus.dataset.geojson.dataset.AbstractReferenceResolvingGeoJsonDataset;
import net.buildtheearth.terraminusminus.dataset.geojson.dataset.ParsingGeoJsonDataset;
import net.buildtheearth.terraminusminus.dataset.geojson.geometry.Point;
import net.buildtheearth.terraminusminus.dataset.geojson.object.Feature;
import net.buildtheearth.terraminusminus.dataset.geojson.object.Reference;
import net.buildtheearth.terraminusminus.dataset.osm.OSMMapper;
import net.buildtheearth.terraminusminus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.daporkchop.lib.common.util.PorkUtil;

/**
 * @author DaPorkchop_
 */
@Getter
public class GeoJsonToVectorDataset extends AbstractReferenceResolvingGeoJsonDataset<VectorGeometry[]> {
    protected final OSMMapper<Geometry> mapper;
    protected final GeographicProjection earthProjection;

    public GeoJsonToVectorDataset(@NonNull ParsingGeoJsonDataset delegate, @NonNull OSMMapper<Geometry> mapper, @NonNull GeographicProjection earthProjection) {
        super(delegate);

        this.mapper = mapper;
        this.earthProjection = earthProjection;
    }

    @Override
    protected VectorGeometry[] translate(@NonNull Stream<GeoJsonObject> inputs) {
        return inputs.flatMap(object -> this.convertToElements(null, Collections.emptyMap(), object)).toArray(VectorGeometry[]::new);
    }

    @Override
    protected VectorGeometry[] merge(@NonNull Stream<VectorGeometry[]> inputs) {
        return inputs.flatMap(Arrays::stream).toArray(VectorGeometry[]::new);
    }

    protected Stream<VectorGeometry> convertToElements(String id, @NonNull Map<String, String> tags, @NonNull GeoJsonObject object) {
        if (object instanceof Iterable) {
            //recursively process all child elements
            return StreamSupport.stream(PorkUtil.<Iterable<? extends GeoJsonObject>>uncheckedCast(object).spliterator(), false)
                    .flatMap(child -> this.convertToElements(id, tags, child));
        } else if (object instanceof Feature) {
            //process child using properties from feature
            Feature feature = (Feature) object;
            return this.convertToElements(feature.id() != null ? feature.id() : id, feature.properties() != null ? feature.properties() : tags, feature.geometry());
        } else if (object instanceof Point) { //TODO: we currently can't handle points
            return Stream.empty();
        } else if (object instanceof Reference) { //ignore references, they'll be resolved later asynchronously
            return Stream.empty();
        } else {
            try {
                Geometry geometry = (Geometry) object;
                Collection<VectorGeometry> elements = this.mapper.apply(id, tags, geometry, geometry.project(this.earthProjection::fromGeo));
                return elements != null ? elements.stream() : Stream.empty();
            } catch (OutOfProjectionBoundsException e) {//skip element
                return Stream.empty();
            }
        }
    }
}
