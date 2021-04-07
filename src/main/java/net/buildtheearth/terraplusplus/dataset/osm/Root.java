package net.buildtheearth.terraplusplus.dataset.osm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.LineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPoint;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.Point;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.Polygon;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.line.LineMapper;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon.PolygonMapper;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.daporkchop.lib.common.util.PorkUtil;

import java.util.Collection;
import java.util.Map;

/**
 * Root of the OpenStreetMap configuration.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
@JsonSerialize
final class Root implements OSMMapper<Geometry> {
    protected final LineMapper line;
    protected final PolygonMapper polygon;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Root(
            @JsonProperty(value = "line", required = true) @NonNull LineMapper line,
            @JsonProperty(value = "polygon", required = true) @NonNull PolygonMapper polygon) {
        this.line = line;
        this.polygon = polygon;
    }

    @Override
    public Collection<VectorGeometry> apply(String id, @NonNull Map<String, String> tags, @NonNull Geometry originalGeometry, @NonNull Geometry projectedGeometry) {
        if (projectedGeometry instanceof Point || projectedGeometry instanceof MultiPoint) { //points can't be generated
            return null;
        }

        //convert to multi type if not already
        if (projectedGeometry instanceof LineString) {
            projectedGeometry = new MultiLineString(new LineString[]{ (LineString) projectedGeometry });
        } else if (projectedGeometry instanceof Polygon) {
            projectedGeometry = new MultiPolygon(new Polygon[]{ (Polygon) projectedGeometry });
        }

        if (projectedGeometry instanceof MultiLineString) {
            return this.line.apply(id, tags, originalGeometry, (MultiLineString) projectedGeometry);
        } else if (projectedGeometry instanceof MultiPolygon) {
            return this.polygon.apply(id, tags, originalGeometry, (MultiPolygon) projectedGeometry);
        } else {
            throw new IllegalArgumentException("unsupported geometry type: " + PorkUtil.className(projectedGeometry));
        }
    }
}
