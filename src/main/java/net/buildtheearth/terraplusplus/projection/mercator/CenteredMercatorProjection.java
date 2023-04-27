package net.buildtheearth.terraplusplus.projection.mercator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.Getter;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.sis.WKTStandard;
import net.buildtheearth.terraplusplus.util.TerraUtils;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Implementation of the Mercator projection, normalized between -1 and 1.
 * <p>
 * DaPorkchop_ says: you dummies, this isn't actually proper Mercator on an ellipsoid; it's spherical Mercator (aka "Pseudo-Mercator"), and is
 * identical to {@link WebMercatorProjection} except that it's normalized to a different range and flipped.
 *
 * @see WebMercatorProjection
 * @see <a href="https://en.wikipedia.org/wiki/Mercator_projection"> Wikipedia's article on the Mercator projection</a>
 */
@JsonDeserialize
public class CenteredMercatorProjection implements GeographicProjection {
    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(x, y, 1, 1);
        return new double[]{
                x * 180.0,
                Math.toDegrees(Math.atan(Math.exp(-y * Math.PI)) * 2 - Math.PI / 2)
        };
    }

    @Override
    public Matrix2 toGeoDerivative(double x, double y) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(x, y, 1, 1);

        double m00 = 180.0d;
        double m01 = 0.0d;
        double m10 = 0.0d;

        //https://www.wolframalpha.com/input?i=d%2Fdy+%28%28atan%28exp%28-y+*+pi%29%29+*+2+-+pi%2F2%29+*+2+-+pi+%2F+2%29+*+180+%2F+pi
        double m11 = (-360.0d * Math.exp(y * Math.PI)) / (Math.exp(2.0d * Math.PI * y) + 1.0d);

        return new Matrix2(m00, m01, m10, m11);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(longitude, latitude, 180, WebMercatorProjection.LIMIT_LATITUDE);
        return new double[]{
                longitude / 180.0,
                -(Math.log(Math.tan((Math.PI / 2 + Math.toRadians(latitude)) / 2))) / Math.PI
        };
    }

    @Override
    public Matrix2 fromGeoDerivative(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(longitude, latitude, 180, WebMercatorProjection.LIMIT_LATITUDE);

        double m00 = 1.0d / 180.0d;
        double m01 = 0.0d;
        double m10 = 0.0d;

        //https://www.wolframalpha.com/input?i=d%2Fdy+-%28log%28tan%28%28pi+%2F+2+%2B+%28y+%2F+180+*+pi%29%29+%2F+2%29%29%29+%2F+pi
        double m11 = -1.0d / (360.0d * Math.cos((90.0d + latitude) * Math.PI / 360.0d) * Math.sin((90.0d + latitude) * Math.PI / 360.0d));

        return new Matrix2(m00, m01, m10, m11);
    }

    @Override
    public double[] bounds() {
        return new double[]{ -1, -1, 1, 1 };
    }

    @Override
    public boolean upright() {
        return true;
    }

    @Override
    public String toString() {
        return "Mercator";
    }
}
