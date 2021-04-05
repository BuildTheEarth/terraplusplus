package net.buildtheearth.terraplusplus.dataset.scalar.tile.mode;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "name")
@JsonTypeIdResolver(TileMode.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface TileMode {
    /**
     * @return the path to the tile at the given position
     */
    String path(int tileX, int tileZ, int zoom);

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<TileMode> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.TILE_MODES);
        }
    }
}
