package net.buildtheearth.terraplusplus.dataset.vector.draw;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;

/**
 * {@link DrawFunction} which removes trees at a given position.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class DrawFunctionNoTrees implements DrawFunction {
    @Override
    public void drawOnto(@NonNull CachedChunkData.Builder data, int x, int z, int weight) {
        byte[] treeCover = data.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, null);
        if (treeCover != null) {
            treeCover[x * 16 + z] = (byte) 0; //set chance to 0
        }
    }
}
