package net.buildtheearth.terraplusplus.projection.mercator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.TerraUtils;

/**
 * Implementation of the web Mercator projection, with projected space normalized between 0 and 2^zoom * 256.
 * This projection is mainly used by tiled mapping services like GoogleMaps or OpenStreetMap.
 * In this implementation of the projection, 1 unit on the projected space corresponds to 1 pixel on those services at the same zoom level.
 * The origin is on the upper left corner of the map.
 *
 * @see CenteredMercatorProjection
 * @see <a href="https://en.wikipedia.org/wiki/Web_Mercator_projection"> Wikipedia's article on the Web Mercator projection</a>
 */
@JsonDeserialize
public class WebMercatorProjection implements GeographicProjection {
    public static final double LIMIT_LATITUDE = Math.toDegrees(2 * Math.atan(Math.pow(Math.E, Math.PI)) - Math.PI / 2);

    public static final double SCALE_FROM = 256.0d;
    public static final double SCALE_TO = 1.0d / SCALE_FROM;

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        if (x < 0 || y < 0 || x > SCALE_FROM || y > SCALE_FROM) {
            throw OutOfProjectionBoundsException.get();
        }
        return new double[]{
                Math.toDegrees(SCALE_TO * x * TerraUtils.TAU - Math.PI),
                Math.toDegrees(Math.atan(Math.exp(Math.PI - SCALE_TO * y * TerraUtils.TAU)) * 2 - Math.PI / 2)
        };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(longitude, latitude, 180, LIMIT_LATITUDE);
        return new double[]{
                SCALE_FROM * (Math.toRadians(longitude) + Math.PI) / TerraUtils.TAU,
                SCALE_FROM * (Math.PI - Math.log(Math.tan((Math.PI / 2 + Math.toRadians(latitude)) / 2))) / TerraUtils.TAU
        };
    }

    @Override
    public double[] bounds() {
        return new double[]{ 0, 0, SCALE_FROM, SCALE_FROM };
    }

    @Override
    public double[] boundsGeo() {
        return new double[]{ -180.0d, -LIMIT_LATITUDE, 180.0d, LIMIT_LATITUDE };
    }

    @Override
    public boolean upright() {
        return true;
    }

    @Override
    public double metersPerUnit() {
        return 100000;
    }

    @Override
    public String toString() {
        return "Web Mercator";
    }
}
