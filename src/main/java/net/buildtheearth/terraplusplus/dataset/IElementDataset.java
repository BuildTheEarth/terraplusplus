package net.buildtheearth.terraplusplus.dataset;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;

import java.lang.reflect.Array;
import java.util.concurrent.CompletableFuture;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * A dataset consisting of arbitrary elements.
 *
 * @author DaPorkchop_
 */
public interface IElementDataset<V> {
    /**
     * Gets an {@link IElementDataset} which contains no elements.
     *
     * @param type the class of the element type
     * @return an {@link IElementDataset} which contains no elements
     */
    static <V> IElementDataset<V> empty(@NonNull Class<V> type) {
        return bounds -> CompletableFuture.completedFuture(uncheckedCast(Array.newInstance(type, 0)));
    }

    /**
     * Gets all of the elements that intersect the given bounding box.
     *
     * @param bounds the bounding box
     * @return a {@link CompletableFuture} which will be completed with the elements
     */
    CompletableFuture<V[]> getAsync(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException;
}
