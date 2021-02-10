package net.buildtheearth.terraplusplus.dataset;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * A dataset consisting of arbitrary elements.
 *
 * @author DaPorkchop_
 */
public interface IElementDataset<V> {
    /**
     * Gets all of the elements that intersect the given bounding box.
     *
     * @param bounds the bounding box
     * @return a {@link CompletableFuture} which will be completed with the elements
     */
    CompletableFuture<V[]> getAsync(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException;
}
