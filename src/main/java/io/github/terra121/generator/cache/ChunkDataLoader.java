package io.github.terra121.generator.cache;

import com.google.common.cache.CacheLoader;
import io.github.terra121.generator.EarthGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;

import static java.lang.Math.*;

@RequiredArgsConstructor
public class ChunkDataLoader extends CacheLoader<ChunkPos, CachedChunkData> {
    @NonNull
    protected final EarthGenerator generator;

    @Override
    public CachedChunkData load(ChunkPos pos) throws Exception {
        CachedChunkData data = new CachedChunkData();

        if (abs(pos.x) < 5 && abs(pos.z) < 5) { //null island
            Arrays.fill(data.heights, 1.0d);
        } else {
            //get heights beforehand
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double[] projected = this.generator.projection.toGeo(pos.x * 16 + x, pos.z * 16 + z);
                    data.heights[x * 16 + z] = this.generator.heights.estimateLocal(projected[0], projected[1]);
                    data.wateroffs[x * 16 + z] = this.generator.osm.water.estimateLocal(projected[0], projected[1]);
                }
            }
        }
        return data;
    }
}
