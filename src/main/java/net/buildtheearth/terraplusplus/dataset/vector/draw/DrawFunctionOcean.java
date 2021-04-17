package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;

/**
 * {@link DrawFunction} which updates the water depth based on the pixel weight.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class DrawFunctionOcean implements DrawFunction {
    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        data.updateOceanDepth(x, z, weight);
    }
}
