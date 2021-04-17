package net.buildtheearth.terraplusplus.dataset.scalar.tile.format;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;

/**
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "name")
@JsonTypeIdResolver(TileFormat.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface TileFormat {
    /**
     * Parses the data stored in the given buffer into a {@code double[]}.
     *
     * @param buf        the data
     * @param resolution the expected resolution of the data
     * @return the parsed data
     */
    double[] parse(@NonNull ByteBuf buf, int resolution);

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<TileFormat> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.TILE_FORMATS);
        }
    }
}
