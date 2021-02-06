package io.github.terra121.dataset.vector;

import io.github.terra121.dataset.IDataset;
import io.github.terra121.dataset.IElementDataset;
import io.github.terra121.dataset.TiledDataset;
import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.projection.EquirectangularProjection;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.BVH;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.util.PorkUtil.*;

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
        return this.delegate.getAsync(String.format("tile/%d/%d.json", key.x, key.z)).thenApply(BVH::of);
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
