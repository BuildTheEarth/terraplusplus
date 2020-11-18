package io.github.terra121.dataset;

import io.github.terra121.projection.GeographicProjection;

import java.util.Iterator;

/**
 * A {@link TiledDataset} which operates on a grid of interpolated {@code double}s.
 *
 * @author DaPorkchop_
 */
public abstract class DoubleTiledDataset extends TiledDataset<double[]> {
    protected static final int TILE_SIZE = 256;

    public final boolean smooth;

    public DoubleTiledDataset(int width, int height, int numcache, GeographicProjection proj, double projScaleX, double projScaleY, boolean smooth) {
        super(width, height, numcache, proj, projScaleX, projScaleY);

        this.smooth = smooth;
    }

    public DoubleTiledDataset(int width, int height, int numcache, GeographicProjection proj, double projScaleX, double projScaleY) {
        this(width, height, numcache, proj, projScaleX, projScaleY, false);
    }

    public double estimateLocal(double lon, double lat, boolean lidar) {
        //basic bound check
        if (!(lon <= 180 && lon >= -180 && lat <= 85 && lat >= -85)) {
            return -2;
        }

        //project coords
        double[] floatCoords = this.projection.fromGeo(lon, lat);

        if (this.smooth) {
            return this.estimateSmooth(floatCoords, lidar);
        }
        return this.estimateBasic(floatCoords, lidar);
    }

    //new style
    protected double estimateSmooth(double[] floatCoords, boolean lidar) {

        double X = floatCoords[0] * this.scaleX - 0.5;
        double Y = floatCoords[1] * this.scaleY - 0.5;

        //get the corners surrounding this block
        Coord coord = new Coord((int) X, (int) Y);

        double u = X - coord.x;
        double v = Y - coord.y;

        double v00 = this.getOfficialHeight(coord, lidar);
        coord.x++;
        double v10 = this.getOfficialHeight(coord, lidar);
        coord.x++;
        double v20 = this.getOfficialHeight(coord, lidar);
        coord.y++;
        double v21 = this.getOfficialHeight(coord, lidar);
        coord.x--;
        double v11 = this.getOfficialHeight(coord, lidar);
        coord.x--;
        double v01 = this.getOfficialHeight(coord, lidar);
        coord.y++;
        double v02 = this.getOfficialHeight(coord, lidar);
        coord.x++;
        double v12 = this.getOfficialHeight(coord, lidar);
        coord.x++;
        double v22 = this.getOfficialHeight(coord, lidar);

        if (v00 == -10000000 || v10 == -10000000 || v20 == -10000000 || v21 == -10000000 || v11 == -10000000 || v01 == -10000000 || v02 == -10000000 || v12 == -10000000 || v22 == -10000000) {
            return -10000000; //return error code
        }

        //Compute smooth 9-point interpolation on this block
        double result = SmoothBlend.compute(u, v, v00, v01, v02, v10, v11, v12, v20, v21, v22);

        if (result > 0 && v00 <= 0 && v10 <= 0 && v20 <= 0 && v21 <= 0 && v11 <= 0 && v01 <= 0 && v02 <= 0 && v12 <= 0 && v22 <= 0) {
            return 0; //anti ocean ridges
        }

        return result;
    }

    //old style
    protected double estimateBasic(double[] floatCoords, boolean lidar) {
        double X = floatCoords[0] * this.scaleX;
        double Y = floatCoords[1] * this.scaleY;

        //get the corners surrounding this block
        Coord coord = new Coord((int) X, (int) Y);

        double u = X - coord.x;
        double v = Y - coord.y;

        double ll = this.getOfficialHeight(coord, lidar);
        coord.x++;
        double lr = this.getOfficialHeight(coord, lidar);
        coord.y++;
        double ur = this.getOfficialHeight(coord, lidar);
        coord.x--;
        double ul = this.getOfficialHeight(coord, lidar);

        if (ll == -10000000 || lr == -10000000 || ur == -10000000 || ul == -10000000) {
            return -10000000;
        }

        //get perlin style interpolation on this block
        return (1 - v) * (ll * (1 - u) + lr * u) + (ul * (1 - u) + ur * u) * v;
    }

    protected double getOfficialHeight(Coord coord, boolean lidar) {

        Coord tile = coord.tile();

        //proper bound check for x
        if (coord.x <= this.bounds[0] || coord.x >= this.bounds[2]) {
            return 0;
        }

        //is the tile that this coord lies on already downloaded?
        int[] img = this.cache.get(tile);

        if (img == null) {
            //download tile
            img = this.request(tile, lidar);
            this.cache.put(tile, img); //save to cache cause chances are it will be needed again soon

            //cache is too large, remove the least recent element
            if (this.cache.size() > this.numcache) {
                Iterator<?> it = this.cache.values().iterator();
                it.next();
                it.remove();
            }
        }

        //get coord from tile and convert to meters (divide by 256.0)
        double heightreturn = this.dataToDouble(img[this.width * (coord.y % this.height) + coord.x % this.width]);
        if (heightreturn != -10000000) {
            return heightreturn; //return height if not transparent
        }
        return -10000000; //best I can think of, returns error code
    }
}
