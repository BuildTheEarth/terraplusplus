package net.buildtheearth.terraplusplus.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(LineParser.class)
@FunctionalInterface
public interface LineMapper extends OSMMapper<MultiLineString> {
}
