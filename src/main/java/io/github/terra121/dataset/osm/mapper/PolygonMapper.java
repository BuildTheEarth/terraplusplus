package io.github.terra121.dataset.osm.mapper;

import com.google.gson.annotations.JsonAdapter;
import io.github.terra121.dataset.geojson.geometry.MultiPolygon;
import io.github.terra121.dataset.osm.OSMMapper;

/**
 * @author DaPorkchop_
 */
@JsonAdapter(PolygonParser.class)
@FunctionalInterface
public interface PolygonMapper extends OSMMapper<MultiPolygon> {
}
