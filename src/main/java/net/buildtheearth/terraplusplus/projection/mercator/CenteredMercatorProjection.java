package net.buildtheearth.terraplusplus.projection.mercator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
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

    @Override
    @SneakyThrows(FactoryException.class)
    public CoordinateReferenceSystem projectedCRS() {
        ReferencingFactoryContainer factories = SISHelper.factories();

        CoordinateSystemAxis[] axes = {
                factories.getCSFactory().createCoordinateSystemAxis(ImmutableMap.of(IdentifiedObject.NAME_KEY, "Easting"), "X", AxisDirection.EAST, Units.METRE),
                factories.getCSFactory().createCoordinateSystemAxis(ImmutableMap.of(IdentifiedObject.NAME_KEY, "Southing"), "Y", AxisDirection.SOUTH, Units.METRE),
        };

        DefaultParameterValueGroup parameters = new DefaultParameterValueGroup(factories.getMathTransformFactory().getDefaultParameters("Popular Visualisation Pseudo Mercator"));
        parameters.getOrCreate(Mercator1SP.SCALE_FACTOR).setValue(4.990640467330674E-8d, Units.UNITY);
        parameters.getOrCreate(MercatorSpherical.FALSE_EASTING).setValue(0.0d, Units.METRE);
        parameters.getOrCreate(MercatorSpherical.FALSE_NORTHING).setValue(0.0d, Units.METRE);

        return factories.getCRSFactory().createProjectedCRS(
                ImmutableMap.of(IdentifiedObject.NAME_KEY, "WGS 84 / Reversed Axis Order / Terra++ Centered Mercator",
                        CoordinateOperation.DOMAIN_OF_VALIDITY_KEY, new SimpleExtent(new DefaultGeographicBoundingBox(-180d, 180d, -90d, 90d), null, null)),
                TPP_GEO_CRS,
                factories.getCoordinateOperationFactory().createDefiningConversion(
                        ImmutableMap.of(IdentifiedObject.NAME_KEY, "Terra++ Centered Mercator"),
                        factories.getCoordinateOperationFactory().getOperationMethod("Popular Visualisation Pseudo Mercator"),
                        parameters),
                factories.getCSFactory().createCartesianCS(
                        ImmutableMap.of(IdentifiedObject.NAME_KEY, AxisDirections.appendTo(new StringBuilder("Cartesian CS"), axes)),
                        axes[0], axes[1]));
    }
}
