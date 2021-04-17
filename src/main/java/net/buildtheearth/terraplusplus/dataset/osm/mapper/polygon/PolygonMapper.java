package net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
@JsonTypeIdResolver(PolygonMapper.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface PolygonMapper extends OSMMapper<MultiPolygon> {
    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<PolygonMapper> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.OSM_POLYGON_MAPPERS);
        }
    }
}
