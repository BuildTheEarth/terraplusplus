package net.buildtheearth.terraplusplus.generator.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.TilePos;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.minecraft.init.Biomes;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static java.lang.Math.*;

/**
 * Sets the surface height at null island to 1.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class DataBakerNullIsland implements IEarthDataBaker<Void> {
    public static final int NULL_ISLAND_RADIUS = 2; //the (square) radius of null island, in chunks

    public static boolean isNullIsland(int chunkZ, int chunkX) {
        return max(chunkZ ^ (chunkZ >> 31), chunkX ^ (chunkX >> 31)) <= NULL_ISLAND_RADIUS;
    }

    @Override
    public CompletableFuture<Void> requestData(TilePos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return null;
    }

    @Override
    public void bake(TilePos pos, CachedChunkData.Builder builder, Void data) {
        if (pos.zoom() == 0) { //optimized implementation for zoom lvl 0
            this.bakeZoom0(pos.x(), pos.z(), builder);
        } else { //slower, more general implementation for higher zooms
            this.bakeZoomHigher(pos.x(), pos.z(), pos.zoom(), builder);
        }
    }

    protected void bakeZoom0(int chunkX, int chunkZ, CachedChunkData.Builder builder) {
        if (isNullIsland(chunkX, chunkZ)) {
            Arrays.fill(builder.surfaceHeight(), -1);

            byte[] trees = builder.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, null);
            if (trees != null) {
                Arrays.fill(trees, (byte) 0);
            }

            Arrays.fill(builder.biomes(), ((chunkX ^ (chunkX >> 31)) | (chunkZ ^ (chunkZ >> 31))) == 0 ? Biomes.FOREST : Biomes.PLAINS);
            Arrays.fill(builder.waterDepth(), (byte) CachedChunkData.WATERDEPTH_DEFAULT);
        }
    }

    protected void bakeZoomHigher(int tileX, int tileZ, int zoom, CachedChunkData.Builder builder) {
        byte[] trees = builder.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, null);

        for (int i = 0, dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++, i++) {
                int chunkX = (tileX << zoom) + Coords.blockToCube(dx << zoom);
                int chunkZ = (tileZ << zoom) + Coords.blockToCube(dz << zoom);
                if (isNullIsland(chunkX, chunkZ)) {
                    builder.surfaceHeight()[i] = -1;

                    if (trees != null) {
                        trees[i] = (byte) 0;
                    }

                    builder.biomes()[i] = ((chunkX ^ (chunkX >> 31)) | (chunkZ ^ (chunkZ >> 31))) == 0 ? Biomes.FOREST : Biomes.PLAINS;
                    builder.waterDepth()[i] = (byte) CachedChunkData.WATERDEPTH_DEFAULT;
                }
            }
        }
    }
}
