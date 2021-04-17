package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.config.GlobalParseRegistries;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;

/**
 * Draws pixels onto a {@link CachedChunkData.Builder} while processing geometry.
 *
 * @author DaPorkchop_
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
@JsonTypeIdResolver(DrawFunction.TypeIdResolver.class)
@JsonDeserialize
@FunctionalInterface
public interface DrawFunction {
    /**
     * Draws a single pixel onto the given {@link CachedChunkData.Builder}.
     *
     * @param data   the {@link CachedChunkData.Builder}
     * @param x      the relative X coordinate of the pixel
     * @param z      the relative Z coordinate of the pixel
     * @param weight the pixel weight
     */
    void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight);

    final class TypeIdResolver extends GlobalParseRegistries.TypeIdResolver<DrawFunction> {
        public TypeIdResolver() {
            super(GlobalParseRegistries.VECTOR_DRAW_FUNCTIONS);
        }
    }
}
