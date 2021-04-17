package net.buildtheearth.terraplusplus.dataset.osm.dvalue;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
@JsonTypeIdResolver(DValue.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface DValue {
    double apply(@NonNull Map<String, String> tags);

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<DValue> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.OSM_DVALUES);
        }
    }
}
