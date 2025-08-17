package net.buildtheearth.terraminusminus.generator.data;

import static java.lang.Math.max;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import net.buildtheearth.terraminusminus.TerraConfig;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.GeneratorDatasets;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.util.CornerBoundingBox2d;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

/**
 * Sets the surface height at null island to 1.
 *
 * @author DaPorkchop_
 */
public class NullIslandBaker implements IEarthDataBaker<Void> {
    @Override
    public CompletableFuture<Void> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return null;
    }

    @Override
    public void bake(ChunkPos pos, CachedChunkData.Builder builder, Void data) {
        if (isNullIsland(pos.x(), pos.z())) {
            Arrays.fill(builder.surfaceHeight(), -1);

            byte[] trees = builder.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, null);
            if (trees != null) {
                Arrays.fill(trees, (byte) 0);
            }

            if (((pos.x() ^ (pos.x() >> 31)) | (pos.z() ^ (pos.z() >> 31))) == 0) {
                Arrays.fill(builder.biomes(), TerraConfig.biomes.nullIslandInnerBiome);
            } else {
                Arrays.fill(builder.biomes(), TerraConfig.biomes.nullIslandOuterBiome);
            }

            Arrays.fill(builder.waterDepth(), (byte) CachedChunkData.WATERDEPTH_DEFAULT);
        }
    }

    public static boolean isNullIsland(int chunkX, int chunkZ) {
        return max(chunkX ^ (chunkX >> 31), chunkZ ^ (chunkZ >> 31)) < 3;
    }
}
