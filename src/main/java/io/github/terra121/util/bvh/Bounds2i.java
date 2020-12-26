package io.github.terra121.util.bvh;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.MathUtils;
import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;

import static java.lang.Math.*;

/**
 * An axis-aligned, 2D bounding box using int-precision floating-point coordinates.
 *
 * @author DaPorkchop_
 */
public interface Bounds2i {
    static Bounds2i of(int x0, int x1, int z0, int z1) {
        return new Bounds2iImpl(min(x0, x1), max(x0, x1), min(z0, z1), max(z0, z1));
    }

    /**
     * @return the minimum X coordinate
     */
    int minX();

    /**
     * @return the maximum X coordinate
     */
    int maxX();

    /**
     * @return the minimum Z coordinate
     */
    int minZ();

    /**
     * @return the maximum Z coordinate
     */
    int maxZ();

    /**
     * Checks whether or not this bounding box intersects the given bounding box.
     *
     * @param other the bounding box
     * @return whether or not the two bounding boxes intersect
     */
    default boolean intersects(@NonNull Bounds2i other) {
        return this.minX() <= other.maxX() && this.maxX() >= other.minX() && this.minZ() <= other.maxZ() && this.maxZ() >= other.minZ();
    }

    /**
     * Checks whether or not this bounding box contains the given bounding box.
     *
     * @param other the bounding box
     * @return whether or not this bounding box contains the given bounding box
     */
    default boolean contains(@NonNull Bounds2i other) {
        return this.minX() <= other.minX() && this.maxX() >= other.maxX() && this.minZ() <= other.minZ() && this.maxZ() >= other.maxZ();
    }

    /**
     * Assuming this bounding box is located on a grid of square tiles, gets the positions of every tile that intersects this bounding box.
     *
     * @param size the side length of a tile
     * @return the positions of every tile that intersects this bounding box
     */
    default ChunkPos[] toTiles(int size) {
        int minXi = floorDiv(this.minX(), size);
        int maxXi = floorDiv(this.maxX(), size);
        int minZi = floorDiv(this.minZ(), size);
        int maxZi = floorDiv(this.maxZ(), size);

        ChunkPos[] out = new ChunkPos[(maxXi - minXi + 1) * (maxZi - minZi + 1)];
        for (int i = 0, x = minXi; x <= maxXi; x++) {
            for (int z = minZi; z <= maxZi; z++) {
                out[i++] = new ChunkPos(x, z);
            }
        }
        return out;
    }

    /**
     * Expands this bounding box by the given amount in every direction.
     *
     * @param offset the amount to expand the bounding box by
     * @return the expanded bounding box
     */
    default Bounds2i expand(int offset) {
        return of(this.minX() - offset, this.maxX() + offset, this.minZ() - offset, this.maxZ() + offset);
    }

    /**
     * Converts this bounding box to an equivalent {@link CornerBoundingBox2d}.
     *
     * @param proj the {@link GeographicProjection} to use
     * @param geo  whether or not this bounding box uses geographic coordinates
     * @return a {@link CornerBoundingBox2d} equivalent to this bounding box
     */
    default CornerBoundingBox2d toCornerBB(@NonNull GeographicProjection proj, boolean geo) throws OutOfProjectionBoundsException {
        int minX = this.minX();
        int minZ = this.minZ();
        return new CornerBoundingBox2d(minX, minZ, this.maxX() - minX, this.maxZ() - minZ, proj, geo);
    }

    /**
     * Ensures that this bounding box is entirely within valid projection bounds.
     *
     * @param proj the {@link GeographicProjection} to use
     * @param geo  whether or not this bounding box uses geographic coordinates
     * @throws OutOfProjectionBoundsException if any part of this bounding box is out of valid projection bounds
     */
    default Bounds2i validate(@NonNull GeographicProjection proj, boolean geo) throws OutOfProjectionBoundsException {
        int minX = this.minX();
        int maxX = this.maxX();
        int minZ = this.minZ();
        int maxZ = this.maxZ();

        if (geo) { //validate bounds
            proj.fromGeo(minX, minZ);
            proj.fromGeo(minX, maxZ);
            proj.fromGeo(maxX, minZ);
            proj.fromGeo(maxX, maxZ);
        } else {
            proj.toGeo(minX, minZ);
            proj.toGeo(minX, maxZ);
            proj.toGeo(maxX, minZ);
            proj.toGeo(maxX, maxZ);
        }
        return this;
    }
}
