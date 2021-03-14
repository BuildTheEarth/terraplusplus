package net.buildtheearth.terraminusminus.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;

import net.buildtheearth.terraminusminus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraminusminus.dataset.osm.OSMMapper;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(LineParser.class)
@FunctionalInterface
public interface LineMapper extends OSMMapper<MultiLineString> {
}
