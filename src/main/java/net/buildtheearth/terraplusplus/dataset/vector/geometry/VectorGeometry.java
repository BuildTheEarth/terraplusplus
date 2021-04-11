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
     * Modifies the given {@link CachedChunkData.Builder} for the given tile.
     *
     * @param builder the {@link CachedChunkData.Builder}
     * @param tileX   the tile's X coordinate
     * @param tileZ   the tile's Z coordinate
     * @param zoom    the tile's zoom level
     * @param bounds  the tile's bounding box
     */
    void apply(@NonNull CachedChunkData.Builder builder, int tileX, int tileZ, int zoom, @NonNull Bounds2d bounds);

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
