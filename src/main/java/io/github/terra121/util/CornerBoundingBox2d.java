package io.github.terra121.util;

import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import net.minecraft.util.math.ChunkPos;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A 2D bounding box consisting of 4 corners.
 *
 * @author DaPorkchop_
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class CornerBoundingBox2d implements Bounds2d {
    protected final double lon00;
    protected final double lon01;
    protected final double lon10;
    protected final double lon11;
    protected final double lat00;
    protected final double lat01;
    protected final double lat10;
    protected final double lat11;

    @NonNull
    protected final GeographicProjection proj;
    protected final boolean geo;

    public CornerBoundingBox2d(@NonNull GeographicProjection proj, double x, double z, double sizeX, double sizeZ) throws OutOfProjectionBoundsException {
        this(proj.toGeo(x, z), proj.toGeo(x, z + sizeZ), proj.toGeo(x + sizeX, z), proj.toGeo(x + sizeX, z + sizeZ), proj, true);
    }

    public CornerBoundingBox2d(@NonNull double[] point00, @NonNull double[] point01, @NonNull double[] point10, @NonNull double[] point11, @NonNull GeographicProjection proj, boolean geo) {
        this.lon00 = point00[0];
        this.lat00 = point00[1];
        this.lon01 = point01[0];
        this.lat01 = point01[1];
        this.lon10 = point10[0];
        this.lat10 = point10[1];
        this.lon11 = point11[0];
        this.lat11 = point11[1];
        this.proj = proj;
        this.geo = geo;
    }

    /**
     * Gets the coordinates of a point in this bounding box.
     * <p>
     * The relative coordinates are both given in range {@code [0-1]}. Providing relative coordinates outside of this range
     * will produce unexpected results.
     *
     * @param dst the {@code double[]} to attempt to store the point in
     * @param fx  the relative position of the point along the X axis
     * @param fz  the relative position of the point along the Z axis
     * @return a {@code double[]} which contains the point (possibly the same as {@code dst})
     */
    public double[] point(double[] dst, double fx, double fz) {
        if (dst == null || dst.length < 2) {
            dst = new double[2];
        }

        dst[0] = lerp(lerp(this.lon00, this.lon01, fz), lerp(this.lon10, this.lon11, fz), fx);
        dst[1] = lerp(lerp(this.lat00, this.lat01, fz), lerp(this.lat10, this.lat11, fz), fx);
        return dst;
    }

    public Bounds2d axisAlign(double minOffset, double maxOffset) throws OutOfProjectionBoundsException {
        double minX = this.minX() + minOffset;
        double maxX = this.maxX() + maxOffset;
        double minZ = this.minZ() + minOffset;
        double maxZ = this.maxZ() + maxOffset;

        //ensure that all points are within projection bounds
        if (this.geo) {
            this.proj.fromGeo(minX, minZ);
            this.proj.fromGeo(minX, maxZ);
            this.proj.fromGeo(maxX, minZ);
            this.proj.fromGeo(maxX, maxZ);
        } else {
            this.proj.toGeo(minX, minZ);
            this.proj.toGeo(minX, maxZ);
            this.proj.toGeo(maxX, minZ);
            this.proj.toGeo(maxX, maxZ);
        }

        return Bounds2d.of(minX, maxX, minZ, maxZ);
    }

    public CornerBoundingBox2d toGeo(@NonNull GeographicProjection proj) throws OutOfProjectionBoundsException {
        checkState(!this.geo, "already in geographic coordinates!");
        return new CornerBoundingBox2d(
                proj.toGeo(this.lon00, this.lat00),
                proj.toGeo(this.lon01, this.lat01),
                proj.toGeo(this.lon10, this.lat10),
                proj.toGeo(this.lon11, this.lat11),
                proj, true);
    }

    public CornerBoundingBox2d fromGeo(@NonNull GeographicProjection proj) throws OutOfProjectionBoundsException {
        checkState(this.geo, "already in local coordinates!");
        return new CornerBoundingBox2d(
                proj.fromGeo(this.lon00, this.lat00),
                proj.fromGeo(this.lon01, this.lat01),
                proj.fromGeo(this.lon10, this.lat10),
                proj.fromGeo(this.lon11, this.lat11),
                proj, false);
    }

    @Override
    public double minX() {
        return min(min(this.lon00, this.lon01), min(this.lon10, this.lon11));
    }

    @Override
    public double maxX() {
        return max(max(this.lon00, this.lon01), max(this.lon10, this.lon11));
    }

    @Override
    public double minZ() {
        return min(min(this.lat00, this.lat01), min(this.lat10, this.lat11));
    }

    @Override
    public double maxZ() {
        return max(max(this.lat00, this.lat01), max(this.lat10, this.lat11));
    }
}
