package net.buildtheearth.terraplusplus.projection.sis;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.util.compat.sis.SISHelper;
import org.apache.sis.internal.referencing.AxisDirections;
import org.apache.sis.internal.referencing.ReferencingFactoryContainer;
import org.apache.sis.internal.simple.SimpleExtent;
import org.apache.sis.measure.Units;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.util.FactoryException;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;

/**
 * @author DaPorkchop_
 */
public abstract class AbstractSISMigratedGeographicProjection implements GeographicProjection {
    @Override
    @SneakyThrows(FactoryException.class)
    public CoordinateReferenceSystem projectedCRS() {
        String name = this.toString();
        double[] bounds = this.boundsGeo();

        ReferencingFactoryContainer factories = SISHelper.factories();

        CoordinateSystemAxis[] axes = {
                factories.getCSFactory().createCoordinateSystemAxis(ImmutableMap.of(IdentifiedObject.NAME_KEY, "Easting"), "X", AxisDirection.EAST, Units.METRE),
                factories.getCSFactory().createCoordinateSystemAxis(ImmutableMap.of(IdentifiedObject.NAME_KEY, "Northing"), "Y", AxisDirection.NORTH, Units.METRE),
        };

        return factories.getCRSFactory().createProjectedCRS(
                ImmutableMap.of(IdentifiedObject.NAME_KEY, "WGS 84 / Reversed Axis Order / Terra++ " + name,
                        CoordinateOperation.DOMAIN_OF_VALIDITY_KEY, new SimpleExtent(new DefaultGeographicBoundingBox(bounds[0], bounds[2], bounds[1], bounds[3]), null, null)),
                TPP_GEO_CRS,
                factories.getCoordinateOperationFactory().createDefiningConversion(
                        ImmutableMap.of(IdentifiedObject.NAME_KEY, "Terra++ " + name),
                        factories.getCoordinateOperationFactory().getOperationMethod("Terra++ " + name),
                        factories.getMathTransformFactory().getDefaultParameters("Terra++" + name)),
                factories.getCSFactory().createCartesianCS(
                        ImmutableMap.of(IdentifiedObject.NAME_KEY, AxisDirections.appendTo(new StringBuilder("Cartesian CS"), axes)),
                        axes[0], axes[1]));
    }
}
