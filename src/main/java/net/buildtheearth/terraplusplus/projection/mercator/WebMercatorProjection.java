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

    //TODO: figure out if this will break anything
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final CoordinateReferenceSystem PROJECTED_CRS = (CoordinateReferenceSystem) WKTStandard.WKT2_2015.parseUnchecked(
            //based on EPSG:3857
            "PROJCRS[\"WGS 84 / Terra++ Scaled Pseudo-Mercator (Web Mercator)\",\n"
            + "    BASEGEODCRS[\"WGS 84\",\n"
            + "        DATUM[\"World Geodetic System 1984\",\n"
            + "            ELLIPSOID[\"WGS 84\",6378137,298.257223563,\n"
            + "                LENGTHUNIT[\"metre\",1]]],\n"
            + "        PRIMEM[\"Greenwich\",0,\n"
            + "            ANGLEUNIT[\"degree\",0.0174532925199433]]],\n"
            + "    CONVERSION[\"unnamed\",\n"
            + "        METHOD[\"Popular Visualisation Pseudo Mercator\",\n"
            + "            ID[\"EPSG\",1024]],\n"
            + "        PARAMETER[\"Latitude of natural origin\",0,\n"
            + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
            + "            ID[\"EPSG\",8801]],\n"
            + "        PARAMETER[\"Longitude of natural origin\",0,\n"
            + "            ANGLEUNIT[\"degree\",0.0174532925199433],\n"
            + "            ID[\"EPSG\",8802]],\n"
            //porkman added this: begin
            + "        PARAMETER[\"Scale factor at natural origin\",6.388019798183263E-6,\n"
            + "            SCALEUNIT[\"unity\",1],\n"
            + "            ID[\"EPSG\",8805]],\n"
            //porkman added this: end
            //porkman changed these parameter values from 0 to 128: begin
            + "        PARAMETER[\"False easting\",128.0,\n"
            + "            LENGTHUNIT[\"metre\",1],\n"
            + "            ID[\"EPSG\",8806]],\n"
            + "        PARAMETER[\"False northing\",-128.0,\n"
            + "            LENGTHUNIT[\"metre\",1],\n"
            + "            ID[\"EPSG\",8807]]],\n"
            //porkman changed these parameter values from 0 to 128: end
            + "    CS[Cartesian,2],\n"
            + "        AXIS[\"easting (X)\",east,\n"
            + "            ORDER[1],\n"
            + "            LENGTHUNIT[\"metre\",1]],\n"
            + "        AXIS[\"southing (Y)\",south,\n"
            + "            ORDER[2],\n"
            + "            LENGTHUNIT[\"metre\",1]],\n"
            + "    SCOPE[\"Web mapping and visualisation.\"],\n"
            + "    AREA[\"World between 85.06°S and 85.06°N.\"],\n"
            + "    BBOX[-85.06,-180,85.06,180]]");

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
    public Matrix2 toGeoDerivative(double x, double y) throws OutOfProjectionBoundsException {
        if (x < 0 || y < 0 || x > SCALE_FROM || y > SCALE_FROM) {
            throw OutOfProjectionBoundsException.get();
        }

        double m00 = Math.toDegrees(SCALE_TO * TerraUtils.TAU);
        double m01 = 0.0d;
        double m10 = 0.0d;

        //https://www.wolframalpha.com/input?i=deriv+%28atan%28exp%28pi+-+y+*+s+*+2+*+pi%29%29+*+2+-+pi%2F2%29+*+180+%2F+pi
        double m11 = (-720.0d * SCALE_TO * Math.exp(TerraUtils.TAU * SCALE_TO * y + Math.PI)) / (Math.exp(4.0d * Math.PI * SCALE_TO * y) + Math.exp(2.0d * Math.PI));

        return new Matrix2(m00, m01, m10, m11);
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
    public Matrix2 fromGeoDerivative(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(longitude, latitude, 180, LIMIT_LATITUDE);

        double m00 = SCALE_FROM * Math.toRadians(1.0d) / TerraUtils.TAU;
        double m01 = 0.0d;
        double m10 = 0.0d;

        //https://www.wolframalpha.com/input?i=d%2Fdl+s+*+%28pi+-+log%28tan%28%28pi+%2F+2+%2B+%28l+%2F180+*+pi%29%29+%2F+2%29%29%29+%2F+%282+*+pi%29
        double m11 = -SCALE_FROM / (720.0d * Math.cos((90.0d + latitude) * Math.PI / 360.0d) * Math.sin((90.0d + latitude) * Math.PI / 360.0d));

        return new Matrix2(m00, m01, m10, m11);
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
    public CoordinateReferenceSystem projectedCRS() {
        return PROJECTED_CRS();
    }

    @Override
    public String toString() {
        return "Web Mercator";
    }
}
