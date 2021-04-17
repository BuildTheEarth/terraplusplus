package net.buildtheearth.terraplusplus.dataset.geojson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.GeometryCollection;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.LineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPoint;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.Point;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.Polygon;
import net.buildtheearth.terraplusplus.dataset.geojson.object.Feature;
import net.buildtheearth.terraplusplus.dataset.geojson.object.FeatureCollection;
import net.buildtheearth.terraplusplus.dataset.geojson.object.Reference;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(Feature.class),
        @JsonSubTypes.Type(FeatureCollection.class),
        @JsonSubTypes.Type(Reference.class),
        @JsonSubTypes.Type(GeometryCollection.class),
        @JsonSubTypes.Type(LineString.class),
        @JsonSubTypes.Type(MultiLineString.class),
        @JsonSubTypes.Type(Point.class),
        @JsonSubTypes.Type(MultiPoint.class),
        @JsonSubTypes.Type(Polygon.class),
        @JsonSubTypes.Type(MultiPolygon.class)
})
@JsonDeserialize
public interface GeoJsonObject {
}
