package net.buildtheearth.terraplusplus.projection.epsg;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.mercator.WebMercatorProjection;
import net.buildtheearth.terraplusplus.util.compat.sis.SISHelper;
import org.apache.sis.internal.referencing.AxisDirections;
import org.apache.sis.internal.referencing.ReferencingFactoryContainer;
import org.apache.sis.internal.referencing.provider.Mercator1SP;
import org.apache.sis.internal.referencing.provider.MercatorSpherical;
import org.apache.sis.internal.simple.SimpleExtent;
import org.apache.sis.measure.Units;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.parameter.DefaultParameterValueGroup;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.util.FactoryException;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;

/**
 * Implementation of the EPSG:3785 projection.
 *
 * @author DaPorkchop_
 * @see <a href="https://epsg.io/3785>https://epsg.io/3785</a>
 * @deprecated this is totally wrong: it actually implements EPSG:3857 (not EPSG:3785), and with incorrect scaling
 */
@Deprecated
public final class EPSG3785 extends EPSGProjection {
    private static final WebMercatorProjection WEB_MERCATOR_PROJECTION = new WebMercatorProjection();

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

    @Override
    @SneakyThrows(FactoryException.class)
    public CoordinateReferenceSystem projectedCRS() {
        //same as WebMercatorProjection, except X axis is northing instead of southing abd false northing is +128 instead of -128

        ReferencingFactoryContainer factories = SISHelper.factories();

        CoordinateSystemAxis[] axes = {
                factories.getCSFactory().createCoordinateSystemAxis(ImmutableMap.of(IdentifiedObject.NAME_KEY, "Easting"), "X", AxisDirection.EAST, Units.METRE),
                factories.getCSFactory().createCoordinateSystemAxis(ImmutableMap.of(IdentifiedObject.NAME_KEY, "Northing"), "Y", AxisDirection.NORTH, Units.METRE),
        };

        DefaultParameterValueGroup parameters = new DefaultParameterValueGroup(factories.getMathTransformFactory().getDefaultParameters("Popular Visualisation Pseudo Mercator"));
        parameters.getOrCreate(Mercator1SP.SCALE_FACTOR).setValue(6.388019798183263E-6d, Units.UNITY);
        parameters.getOrCreate(MercatorSpherical.FALSE_EASTING).setValue(128.0d, Units.METRE);
        parameters.getOrCreate(MercatorSpherical.FALSE_NORTHING).setValue(128.0d, Units.METRE);

        return factories.getCRSFactory().createProjectedCRS(
                ImmutableMap.of(IdentifiedObject.NAME_KEY, "WGS 84 / Reversed Axis Order / Terra++ Incorrect EPSG:3785",
                        CoordinateOperation.DOMAIN_OF_VALIDITY_KEY, new SimpleExtent(new DefaultGeographicBoundingBox(-180d, 180d, -85.06, 85.06), null, null)),
                TPP_GEO_CRS,
                factories.getCoordinateOperationFactory().createDefiningConversion(
                        ImmutableMap.of(IdentifiedObject.NAME_KEY, "Terra++ Incorrect EPSG:3785"),
                        factories.getCoordinateOperationFactory().getOperationMethod("Popular Visualisation Pseudo Mercator"),
                        parameters),
                factories.getCSFactory().createCartesianCS(
                        ImmutableMap.of(IdentifiedObject.NAME_KEY, AxisDirections.appendTo(new StringBuilder("Cartesian CS"), axes)),
                        axes[0], axes[1]));
    }
}
