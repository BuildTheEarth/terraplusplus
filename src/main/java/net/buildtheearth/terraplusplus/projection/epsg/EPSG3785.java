package net.buildtheearth.terraplusplus.projection.epsg;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.mercator.WebMercatorProjection;

/**
 * Implementation of the EPSG:3785 projection.
 *
 * @author DaPorkchop_
 * @see <a href="https://epsg.io/3785>https://epsg.io/3785</a>
 */
public class EPSG3785 extends EPSGProjection {
    protected static final WebMercatorProjection WEB_MERCATOR_PROJECTION = new WebMercatorProjection();

    public EPSG3785() {
        super(3785);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return WEB_MERCATOR_PROJECTION.toGeo(x, 256.0d - y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return WEB_MERCATOR_PROJECTION.fromGeo(longitude, -latitude);
    }

    @Override
    public double[] bounds() {
        return WEB_MERCATOR_PROJECTION.bounds();
    }

    @Override
    public double[] boundsGeo() {
        return WEB_MERCATOR_PROJECTION.boundsGeo();
    }

}
