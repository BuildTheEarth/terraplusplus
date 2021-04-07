package net.buildtheearth.terraplusplus.dataset.osm.mapper.line;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
@JsonTypeIdResolver(LineMapper.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface LineMapper extends OSMMapper<MultiLineString> {
    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<LineMapper> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.OSM_LINE_MAPPERS);
        }
    }
}
