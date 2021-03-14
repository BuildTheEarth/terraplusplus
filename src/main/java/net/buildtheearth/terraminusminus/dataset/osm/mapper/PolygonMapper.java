package net.buildtheearth.terraminusminus.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;

import net.buildtheearth.terraminusminus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraminusminus.dataset.osm.OSMMapper;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(PolygonParser.class)
@FunctionalInterface
public interface PolygonMapper extends OSMMapper<MultiPolygon> {
}
