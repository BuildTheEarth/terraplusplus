package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapper;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapper;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.daporkchop.lib.common.util.GenericMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Returns the combined results of all of a number of mappers, or {@code null} if any one of them returns {@code null}.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@AllArgsConstructor
public abstract class AbstractMapperAll<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @NonNull
    protected final M[] children;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        List<VectorGeometry> out = new ArrayList<>();
        int i = 0;
        for (M child : this.children) {
            Collection<VectorGeometry> result = child.apply(id + '/' + i++, tags, originalGeometry, projectedGeometry);
            if (result == null) { //don't bother processing further children
                return null;
            }
            out.addAll(result);
        }

        return out;
    }
}
