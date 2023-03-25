package net.buildtheearth.terraplusplus.generator.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import net.buildtheearth.terraplusplus.dataset.IElementDataset;
import net.buildtheearth.terraplusplus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.TilePos;
import net.buildtheearth.terraplusplus.util.bvh.BVH;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class DataBakerOSM implements IEarthDataBaker<BVH<VectorGeometry>[]> {
    protected final double paddingRadius;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DataBakerOSM(
            @JsonProperty(value = "paddingRadius", required = false) Double paddingRadius) {
        this.paddingRadius = fallbackIfNull(paddingRadius, 16.0d);
    }

    @Override
    public CompletableFuture<BVH<VectorGeometry>[]> requestData(TilePos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return datasets.<IElementDataset<BVH<VectorGeometry>>>getCustom(EarthGeneratorPipelines.KEY_DATASET_OSM_PARSED)
                .getAsync(bounds.expand(this.paddingRadius).toCornerBB(datasets.projection(), false).toGeo(), pos.zoom());
    }

    @Override
    public void bake(TilePos pos, CachedChunkData.Builder builder, BVH<VectorGeometry>[] regions) {
        if (regions == null) { //there's no data in this chunk... we're going to assume it's completely out of bounds
            Arrays.fill(builder.waterDepth(), (byte) (CachedChunkData.WATERDEPTH_TYPE_OCEAN | ~CachedChunkData.WATERDEPTH_TYPE_MASK));
            return;
        }

        int baseX = pos.blockX();
        int baseZ = pos.blockZ();
        Bounds2d chunkBounds = Bounds2d.of(baseX, baseX + pos.sizeBlocks(), baseZ, baseZ + pos.sizeBlocks());

        Set<VectorGeometry> elements = new TreeSet<>();
        for (BVH<VectorGeometry> region : regions) {
            region.forEachIntersecting(chunkBounds, elements::add);
        }
        elements.forEach(element -> element.apply(builder, pos.x(), pos.z(), pos.zoom(), chunkBounds));
    }
}