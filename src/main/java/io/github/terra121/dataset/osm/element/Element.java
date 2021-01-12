package io.github.terra121.dataset.osm.element;

import io.github.terra121.generator.cache.CachedChunkData;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;

/**
 * A generate-able OpenStreetMap element.
 *
 * @author DaPorkchop_
 */
public interface Element extends Comparable<Element> {
    /**
     * Modifies the given {@link CachedChunkData.Builder} for the given chunk
     *  @param builder      the {@link CachedChunkData.Builder}
     * @param chunkX       the chunk's X coordinate
     * @param chunkZ       the chunk's Z coordinate
     * @param bounds       the chunk's bounding box
     */
    void apply(@NonNull CachedChunkData.Builder builder, int chunkX, int chunkZ, @NonNull Bounds2d bounds);

    /**
     * @return the map layer that this element is on
     */
    double layer();

    @Override
    default int compareTo(Element o) {
        return Double.compare(this.layer(), o.layer());
    }
}
