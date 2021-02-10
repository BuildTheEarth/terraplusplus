package io.github.terra121.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;
import io.github.terra121.dataset.geojson.geometry.MultiLineString;
import io.github.terra121.dataset.osm.OSMMapper;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(LineParser.class)
@FunctionalInterface
public interface LineMapper extends OSMMapper<MultiLineString> {
}