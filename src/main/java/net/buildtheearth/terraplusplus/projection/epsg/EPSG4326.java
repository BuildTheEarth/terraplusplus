package net.buildtheearth.terraplusplus.projection.epsg;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.TerraConstants;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Implementation of the EPSG:4326 projection.
 *
 * @author DaPorkchop_
 * @see <a href="https://epsg.io/4326>https://epsg.io/4326</a>
 */
public final class EPSG4326 extends EPSGProjection {
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
    public CoordinateReferenceSystem projectedCRS() {
        return TerraConstants.TPP_GEO_CRS; //TODO: decide if this should use the real axis order or the flipped one
    }
}
