package io.github.terra121.dataset;

import io.github.terra121.projection.GeographicProjection;

import static io.github.opencubicchunks.cubicchunks.api.util.MathUtil.*;
import static java.lang.Math.*;

/**
 * A {@link TiledDataset} which operates on a grid of interpolated {@code double}s.
 *
 * @author DaPorkchop_
 */
public abstract class DoubleTiledDataset extends TiledDataset<double[]> implements ScalarDataset {
    protected static final int TILE_SHIFT = 8;
    protected static final int TILE_SIZE = 1 << TILE_SHIFT; //256
    protected static final int TILE_MASK = (1 << TILE_SHIFT) - 1; //0xFF

    protected static boolean isInRange(double lon, double lat) {
        return lon <= 180.0d && lon >= -180.0d && lat <= 85.0d && lat >= -85.0d;
    }

    public final boolean smooth;

    public DoubleTiledDataset(GeographicProjection proj, double scale, boolean smooth) {
        super(proj, 1.0d / scale * TILE_SIZE, scale);

        this.smooth = smooth;
    }

    @Override
    public double estimateLocal(double lon, double lat) {
        //basic bound check
        if (!isInRange(lon, lat)) {
            return -2.0d;
        }

        //project coords
        double[] floatCoords = this.projection.fromGeo(lon, lat);

        return this.smooth ? this.estimateSmooth(floatCoords) : this.estimateBasic(floatCoords);
    }

    //new style
    protected double estimateSmooth(double[] floatCoords) {
        double x = floatCoords[0] * this.scale - 0.5d;
        double z = floatCoords[1] * this.scale - 0.5d;

        //get the corners surrounding this block
        //TODO: i think i need to floor() these, not just cast
        int sampleX = (int) floor(x);
        int sampleZ = (int) floor(z);

        double fx = x - sampleX;
        double fz = z - sampleZ;

        double v00 = this.getRawSample(sampleX, sampleZ);
        double v01 = this.getRawSample(sampleX, sampleZ + 1);
        double v02 = this.getRawSample(sampleX, sampleZ + 2);
        double v10 = this.getRawSample(sampleX + 1, sampleZ);
        double v11 = this.getRawSample(sampleX + 1, sampleZ + 1);
        double v12 = this.getRawSample(sampleX + 1, sampleZ + 2);
        double v20 = this.getRawSample(sampleX + 2, sampleZ);
        double v21 = this.getRawSample(sampleX + 2, sampleZ + 1);
        double v22 = this.getRawSample(sampleX + 2, sampleZ + 2);

        //Compute smooth 9-point interpolation on this block
        double result = SmoothBlend.compute(fx, fz, v00, v01, v02, v10, v11, v12, v20, v21, v22);

        if (result > 0.0d && v00 <= 0.0d && v10 <= 0.0d && v20 <= 0.0d && v21 <= 0.0d && v11 <= 0.0d && v01 <= 0.0d && v02 <= 0.0d && v12 <= 0.0d && v22 <= 0) {
            return 0.0d; //anti ocean ridges
        }

        return result;
    }

    //old style
    protected double estimateBasic(double[] floatCoords) {
        double x = floatCoords[0] * this.scale;
        double z = floatCoords[1] * this.scale;

        //get the corners surrounding this block
        int sampleX = (int) floor(x);
        int sampleZ = (int) floor(z);

        double fx = x - sampleX;
        double fz = z - sampleZ;

        double v00 = this.getRawSample(sampleX, sampleZ);
        double v01 = this.getRawSample(sampleX, sampleZ + 1);
        double v10 = this.getRawSample(sampleX + 1, sampleZ);
        double v11 = this.getRawSample(sampleX + 1, sampleZ + 1);

        return lerp(fx, lerp(fz, v00, v01), lerp(fz, v10, v11));
    }

    protected double getRawSample(int sampleX, int sampleZ) {
        if (sampleX <= this.bounds[0] || sampleZ >= this.bounds[2]) {
            return 0.0d;
        }

        double[] tileData = this.getTile(sampleX >> TILE_SHIFT, sampleZ >> TILE_SHIFT);
        return tileData[(sampleZ & TILE_MASK) * TILE_SIZE + (sampleX & TILE_MASK)];
    }
}
