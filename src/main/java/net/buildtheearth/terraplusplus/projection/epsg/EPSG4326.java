package net.buildtheearth.terraplusplus.projection.epsg;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

/**
 * Implementation of the EPSG:4326 projection.
 *
 * @author DaPorkchop_
 * @see <a href="https://epsg.io/4326>https://epsg.io/4326</a>
 */
//TODO: EPSG:4326 isn't a sphere, it uses the WGS84 ellipsoid...
public class EPSG4326 extends EPSGProjection {
    public EPSG4326() {
        super(4326);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return new double[]{ x, y };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return new double[]{ longitude, latitude };
    }

    @Override
    public double metersPerUnit() {
        return 100000.0d;
    }
}
