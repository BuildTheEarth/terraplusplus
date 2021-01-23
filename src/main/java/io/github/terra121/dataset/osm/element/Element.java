package io.github.terra121.dataset.osm.element;

import io.github.terra121.generator.CachedChunkData;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.NonNull;

/**
 * A generate-able OpenStreetMap element.
 *
 * @author DaPorkchop_
 */
public interface Element extends Comparable<Element>, Bounds2d {
    /**
     * Modifies the given {@link CachedChunkData.Builder} for the given chunk
     *
     * @param builder the {@link CachedChunkData.Builder}
     * @param chunkX  the chunk's X coordinate
     * @param chunkZ  the chunk's Z coordinate
     * @param bounds  the chunk's bounding box
     */
    void apply(@NonNull CachedChunkData.Builder builder, int chunkX, int chunkZ, @NonNull Bounds2d bounds);

    /**
     * @return this element's id
     */
    String id();

    /**
     * @return the map layer that this element is on
     */
    double layer();

    @Override
    default int compareTo(Element o) {
        int d = Double.compare(this.layer(), o.layer());
        if (d == 0) { //elements are on the same layer, compare IDs
            return this.id().compareTo(o.id());
        } else {
            return d;
        }
    }
}
