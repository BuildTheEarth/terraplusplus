package net.buildtheearth.terraminusminus.dataset.vector;

import static net.daporkchop.lib.common.util.PorkUtil.uncheckedCast;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.dataset.IDataset;
import net.buildtheearth.terraminusminus.dataset.IElementDataset;
import net.buildtheearth.terraminusminus.dataset.TiledDataset;
import net.buildtheearth.terraminusminus.dataset.vector.geometry.VectorGeometry;
import net.buildtheearth.terraminusminus.projection.EquirectangularProjection;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.util.CornerBoundingBox2d;
import net.buildtheearth.terraminusminus.util.bvh.BVH;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

/**
 * @author DaPorkchop_
 */
public class VectorTiledDataset extends TiledDataset<BVH<VectorGeometry>> implements IElementDataset<BVH<VectorGeometry>> {
    protected final IDataset<String, VectorGeometry[]> delegate;

    public VectorTiledDataset(@NonNull IDataset<String, VectorGeometry[]> delegate) {
        super(new EquirectangularProjection(), 1.0d / 64.0d);

        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<BVH<VectorGeometry>> load(@NonNull ChunkPos key) throws Exception {
        return this.delegate.getAsync(String.format("tile/%d/%d.json", key.x(), key.z())).thenApply(BVH::of);
    }

    @Override
    public CompletableFuture<BVH<VectorGeometry>[]> getAsync(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException {
        Bounds2d localBounds = bounds.fromGeo(this.projection).axisAlign();
        CompletableFuture<BVH<VectorGeometry>>[] futures = uncheckedCast(Arrays.stream(localBounds.toTiles(this.tileSize))
                .map(this::getAsync)
                .toArray(CompletableFuture[]::new));

        return CompletableFuture.allOf(futures).thenApplyAsync(unused ->
                uncheckedCast(Arrays.stream(futures)
                        .map(CompletableFuture::join)
                        .toArray(BVH[]::new)));
    }
}
