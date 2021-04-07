package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapper;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapper;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.osm.JsonParser;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.osm.match.MatchCondition;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.daporkchop.lib.common.util.GenericMatcher;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Forwards elements to another mapper if a given {@link MatchCondition} matches.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@AllArgsConstructor
public abstract class AbstractMapperCondition<G extends Geometry, M extends OSMMapper<G>> implements OSMMapper<G> {
    @NonNull
    protected final MatchCondition match;
    @NonNull
    protected final M emit;

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull G projectedGeometry) {
        if (!this.match.test(id, tags, originalGeometry, projectedGeometry)) { //element doesn't match, emit nothing
            return null;
        }

        return this.emit.apply(id, tags, originalGeometry, projectedGeometry);
    }
}
