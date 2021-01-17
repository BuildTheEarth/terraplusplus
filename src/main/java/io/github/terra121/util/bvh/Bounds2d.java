package io.github.terra121.util.bvh;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import lombok.NonNull;
import net.minecraft.util.math.ChunkPos;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;

/**
 * An axis-aligned, 2D bounding box using double-precision floating-point coordinates.
 *
 * @author DaPorkchop_
 */
public interface Bounds2d {
    static Bounds2d of(double x0, double x1, double z0, double z1) {
        return new Bounds2dImpl(min(x0, x1), max(x0, x1), min(z0, z1), max(z0, z1));
    }

    /**
     * @return the minimum X coordinate
     */
    double minX();

    /**
     * @return the maximum X coordinate
     */
    double maxX();

    /**
     * @return the minimum Z coordinate
     */
    double minZ();

    /**
     * @return the maximum Z coordinate
     */
    double maxZ();

    /**
     * Checks whether or not this bounding box intersects the given bounding box.
     *
     * @param other the bounding box
     * @return whether or not the two bounding boxes intersect
     */
    default boolean intersects(@NonNull Bounds2d other) {
        return this.minX() <= other.maxX() && this.maxX() >= other.minX() && this.minZ() <= other.maxZ() && this.maxZ() >= other.minZ();
    }

    /**
     * Checks whether or not this bounding box contains the given bounding box.
     *
     * @param other the bounding box
     * @return whether or not this bounding box contains the given bounding box
     */
    default boolean contains(@NonNull Bounds2d other) {
        return this.minX() <= other.minX() && this.maxX() >= other.maxX() && this.minZ() <= other.minZ() && this.maxZ() >= other.maxZ();
    }

    /**
     * Assuming this bounding box is located on a grid of square tiles, gets the positions of every tile that intersects this bounding box.
     *
     * @param size the side length of a tile
     * @return the positions of every tile that intersects this bounding box
     */
    default ChunkPos[] toTiles(double size) {
        double invSize = 1.0d / size;
        int minXi = floorI(this.minX() * invSize);
        int maxXi = ceilI(this.maxX() * invSize);
        int minZi = floorI(this.minZ() * invSize);
        int maxZi = ceilI(this.maxZ() * invSize);

        ChunkPos[] out = new ChunkPos[(maxXi - minXi + 1) * (maxZi - minZi + 1)];
        for (int i = 0, x = minXi; x <= maxXi; x++) {
            for (int z = minZi; z <= maxZi; z++) {
                out[i++] = new ChunkPos(x, z);
            }
        }
        return out;
    }

    /**
     * Returns a bounding box that contains this bounding box and the given one.
     *
     * @param other the other bounding box
     * @return the minimum bounding box containing both bounding boxes
     */
    default Bounds2d union(@NonNull Bounds2d other) {
        return of(min(this.minX(), other.minX()), max(this.maxX(), other.maxX()), min(this.minZ(), other.minZ()), max(this.maxZ(), other.maxZ()));
    }

    /**
     * Expands this bounding box by the given amount in every direction.
     *
     * @param offset the amount to expand the bounding box by
     * @return the expanded bounding box
     */
    default Bounds2d expand(double offset) {
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
        double minX = this.minX();
        double minZ = this.minZ();
        return new CornerBoundingBox2d(minX, minZ, this.maxX() - minX, this.maxZ() - minZ, proj, geo);
    }

    /**
     * Ensures that this bounding box is entirely within valid projection bounds.
     *
     * @param proj the {@link GeographicProjection} to use
     * @param geo  whether or not this bounding box uses geographic coordinates
     * @throws OutOfProjectionBoundsException if any part of this bounding box is out of valid projection bounds
     */
    default Bounds2d validate(@NonNull GeographicProjection proj, boolean geo) throws OutOfProjectionBoundsException {
        double minX = this.minX();
        double maxX = this.maxX();
        double minZ = this.minZ();
        double maxZ = this.maxZ();

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
