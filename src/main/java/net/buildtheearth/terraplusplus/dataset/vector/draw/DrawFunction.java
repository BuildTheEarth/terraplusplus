package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.google.gson.annotations.JsonAdapter;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import lombok.NonNull;

/**
 * Draws pixels onto a {@link CachedChunkData.Builder} while processing geometry.
 *
 * @author DaPorkchop_
 */
@JsonAdapter(DrawFunctionParser.class)
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
}
