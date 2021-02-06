package io.github.terra121.dataset;

import io.github.terra121.dataset.vector.geometry.VectorGeometry;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * A dataset consisting of vector geometry.
 *
 * @author DaPorkchop_
 */
public interface IVectorDataset {
    /**
     * Gets all of the geometry elements that intersect the given bounding box.
     *
     * @param bounds the bounding box
     * @return a {@link CompletableFuture} which will be completed with the geometry
     */
    CompletableFuture<VectorGeometry[]> getAsync(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException;
}
