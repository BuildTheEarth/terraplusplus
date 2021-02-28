package net.buildtheearth.terraplusplus.dataset.vector.geometry;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

/**
 * A generate-able OpenStreetMap element.
 *
 * @author DaPorkchop_
 */
public interface VectorGeometry extends Comparable<VectorGeometry>, Bounds2d {
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
    default int compareTo(VectorGeometry o) {
        int d = Double.compare(this.layer(), o.layer());
        if (d == 0) { //elements are on the same layer, compare IDs
            return this.id().compareTo(o.id());
        } else {
            return d;
        }
    }
}
