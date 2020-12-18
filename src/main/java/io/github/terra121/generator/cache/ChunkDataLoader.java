package io.github.terra121.generator.cache;

import com.google.common.cache.CacheLoader;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.terra121.generator.EarthGenerator;
import io.github.terra121.projection.OutOfProjectionBoundsException;
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
    public CachedChunkData load(ChunkPos pos) {
        CachedChunkData data = new CachedChunkData();

        double[] lons = new double[16 * 16];
        double[] lats = new double[16 * 16];

        //project all coordinates
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                try {
                    double[] projected = this.generator.projection.toGeo(pos.x * 16 + x, pos.z * 16 + z);
                    lons[x * 16 + z] = projected[0];
                    lats[x * 16 + z] = projected[1];
                } catch (OutOfProjectionBoundsException e) { //out of bounds
                    lons[x * 16 + z] = Double.NaN;
                    lats[x * 16 + z] = Double.NaN;
                }
            }
        }

        if (abs(pos.x) < 5 && abs(pos.z) < 5) { //null island
            Arrays.fill(data.heights, 1.0d);
        } else {
            //get heights beforehand
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double[] heights = this.generator.heights.getAsync(lons, lats).join();
                    System.arraycopy(heights, 0, data.heights, 0, heights.length);

                    data.wateroffs[x * 16 + z] = this.generator.osm.water.estimateLocal(lons[x * 16 + z], lats[x * 16 + z]);
                }
            }
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 16 * 16; i++) {
            min = Math.min(min, data.heights[i]);
            max = Math.max(max, data.heights[i]);
        }
        data.surfaceMinCube = Coords.blockToCube(min);
        data.surfaceMaxCube = Coords.blockToCube(Math.ceil(max));

        return data;
    }
}
