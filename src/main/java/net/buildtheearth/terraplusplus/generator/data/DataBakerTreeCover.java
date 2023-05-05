package net.buildtheearth.terraplusplus.generator.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.TilePos;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.buildtheearth.terraplusplus.util.geo.pointarray.PointArray2D;

import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class DataBakerTreeCover implements IEarthDataBaker<double[]> {
    public static final double TREE_AREA = 2.0d * 2.0d; //the surface area covered by an average tree

    public static final byte[] FALLBACK_TREE_DENSITY = new byte[16 * 16];

    static {
        //TODO: figure out why i did this:
        // Arrays.fill(FALLBACK_TREE_DENSITY, treeChance(50.0d));
    }

    static byte treeChance(double value) {
        if (Double.isNaN(value)) {
            return 0;
        }

        //value is in range [0-1]
        value *= (1.0 / TREE_AREA);

        //scale to byte range
        value *= 255.0d;

        //increase by 50%
        value *= 1.50d;

        return (byte) clamp(ceilI(value), 0, 255);
    }

    @Override
    public CompletableFuture<double[]> requestData(TilePos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_TREE_COVER).getAsync(boundsGeo, 16, 16);
    }

    @Override
    public void bake(TilePos pos, CachedChunkData.Builder builder, double[] treeCover) {
        byte[] arr = new byte[16 * 16];
        if (treeCover != null) {
            for (int i = 0; i < 16 * 16; i++) {
                arr[i] = treeChance(treeCover[i]);
            }
        }
        builder.putCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, arr);
    }
}
