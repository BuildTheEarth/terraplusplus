package net.buildtheearth.terraplusplus.projection.mercator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.sis.WKTStandard;
import net.buildtheearth.terraplusplus.util.TerraUtils;
import net.buildtheearth.terraplusplus.util.compat.sis.SISHelper;
import org.apache.sis.internal.referencing.AxisDirections;
import org.apache.sis.internal.referencing.ReferencingFactoryContainer;
import org.apache.sis.internal.referencing.provider.Mercator1SP;
import org.apache.sis.internal.referencing.provider.MercatorSpherical;
import org.apache.sis.internal.simple.SimpleExtent;
import org.apache.sis.measure.Units;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.parameter.DefaultParameterValueGroup;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.util.FactoryException;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;

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
    @SneakyThrows(FactoryException.class)
    public CoordinateReferenceSystem projectedCRS() {
        ReferencingFactoryContainer factories = SISHelper.factories();

        CoordinateSystemAxis[] axes = {
                factories.getCSFactory().createCoordinateSystemAxis(ImmutableMap.of(IdentifiedObject.NAME_KEY, "Easting"), "X", AxisDirection.EAST, Units.METRE),
                factories.getCSFactory().createCoordinateSystemAxis(ImmutableMap.of(IdentifiedObject.NAME_KEY, "Southing"), "Y", AxisDirection.SOUTH, Units.METRE),
        };

        DefaultParameterValueGroup parameters = new DefaultParameterValueGroup(factories.getMathTransformFactory().getDefaultParameters("Popular Visualisation Pseudo Mercator"));
        parameters.getOrCreate(Mercator1SP.SCALE_FACTOR).setValue(6.388019798183263E-6d, Units.UNITY);
        parameters.getOrCreate(MercatorSpherical.FALSE_EASTING).setValue(128.0d, Units.METRE);
        parameters.getOrCreate(MercatorSpherical.FALSE_NORTHING).setValue(-128.0d, Units.METRE);

        return factories.getCRSFactory().createProjectedCRS(
                ImmutableMap.of(IdentifiedObject.NAME_KEY, "WGS 84 / Reversed Axis Order / Terra++ Web Mercator",
                        CoordinateOperation.DOMAIN_OF_VALIDITY_KEY, new SimpleExtent(new DefaultGeographicBoundingBox(-180d, 180d, -85.06, 85.06), null, null)),
                TPP_GEO_CRS,
                factories.getCoordinateOperationFactory().createDefiningConversion(
                        ImmutableMap.of(IdentifiedObject.NAME_KEY, "Terra++ Web Mercator"),
                        factories.getCoordinateOperationFactory().getOperationMethod("Popular Visualisation Pseudo Mercator"),
                        parameters),
                factories.getCSFactory().createCartesianCS(
                        ImmutableMap.of(IdentifiedObject.NAME_KEY, AxisDirections.appendTo(new StringBuilder("Cartesian CS"), axes)),
                        axes[0], axes[1]));
    }

    @Override
    public String toString() {
        return "Web Mercator";
    }
}
