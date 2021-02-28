package net.buildtheearth.terraplusplus.util;

import lombok.NonNull;
import lombok.ToString;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A 2D bounding box consisting of 4 corners.
 *
 * @author DaPorkchop_
 */
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

    @ToString.Exclude
    protected CornerBoundingBox2d other;

    public CornerBoundingBox2d(double x, double z, double sizeX, double sizeZ, @NonNull GeographicProjection proj, boolean geo) throws OutOfProjectionBoundsException {
        this.lon00 = x;
        this.lon01 = x;
        this.lon10 = x + sizeX;
        this.lon11 = x + sizeX;
        this.lat00 = z;
        this.lat01 = z + sizeZ;
        this.lat10 = z;
        this.lat11 = z + sizeZ;
        this.proj = proj;
        this.geo = geo;

        this.validate();
    }

    public CornerBoundingBox2d(@NonNull double[] point00, @NonNull double[] point01, @NonNull double[] point10, @NonNull double[] point11, @NonNull GeographicProjection proj, boolean geo) throws OutOfProjectionBoundsException {
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

        this.validate();
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

    /**
     * @return a {@link Bounds2d} which contains the entire area enclosed by this bounding box
     */
    public Bounds2d axisAlign() throws OutOfProjectionBoundsException {
        return Bounds2d.of(this.minX(), this.maxX(), this.minZ(), this.maxZ()).validate(this.proj, this.geo);
    }

    /**
     * @return this bounding box, projected to geographic coordinates
     */
    public CornerBoundingBox2d toGeo() throws OutOfProjectionBoundsException {
        checkState(!this.geo, "already in geographic coordinates!");
        if (this.other == null) {
            (this.other = this.toGeo(this.proj)).other = this;
        }
        return this.other;
    }

    /**
     * @return this bounding box, projected to local coordinates
     */
    public CornerBoundingBox2d fromGeo() throws OutOfProjectionBoundsException {
        checkState(this.geo, "already in local coordinates!");
        if (this.other == null) {
            (this.other = this.fromGeo(this.proj)).other = this;
        }
        return this.other;
    }

    /**
     * @return this bounding box, projected to geographic coordinates using the given {@link GeographicProjection}
     */
    public CornerBoundingBox2d toGeo(@NonNull GeographicProjection proj) throws OutOfProjectionBoundsException {
        checkState(!this.geo, "already in geographic coordinates!");
        return new CornerBoundingBox2d(
                proj.toGeo(this.lon00, this.lat00),
                proj.toGeo(this.lon01, this.lat01),
                proj.toGeo(this.lon10, this.lat10),
                proj.toGeo(this.lon11, this.lat11),
                proj, true);
    }

    /**
     * @return this bounding box, projected to local coordinates using the given {@link GeographicProjection}
     */
    public CornerBoundingBox2d fromGeo(@NonNull GeographicProjection proj) throws OutOfProjectionBoundsException {
        checkState(this.geo, "already in local coordinates!");
        return new CornerBoundingBox2d(
                proj.fromGeo(this.lon00, this.lat00),
                proj.fromGeo(this.lon01, this.lat01),
                proj.fromGeo(this.lon10, this.lat10),
                proj.fromGeo(this.lon11, this.lat11),
                proj, false);
    }

    /**
     * Ensures that this bounding box is entirely within valid projection bounds.
     *
     * @throws OutOfProjectionBoundsException if any part of this bounding box is out of valid projection bounds
     */
    public CornerBoundingBox2d validate() throws OutOfProjectionBoundsException {
        if (this.geo) { //validate bounds
            this.proj.fromGeo(this.lon00, this.lat00);
            this.proj.fromGeo(this.lon01, this.lat01);
            this.proj.fromGeo(this.lon10, this.lat10);
            this.proj.fromGeo(this.lon11, this.lat11);
        } else {
            this.proj.toGeo(this.lon00, this.lat00);
            this.proj.toGeo(this.lon01, this.lat01);
            this.proj.toGeo(this.lon10, this.lat10);
            this.proj.toGeo(this.lon11, this.lat11);
        }
        return this;
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
