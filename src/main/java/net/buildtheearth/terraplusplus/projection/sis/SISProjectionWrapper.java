package net.buildtheearth.terraplusplus.projection.sis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.control.AdvancedEarthGui;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.compat.sis.SISHelper;
import net.daporkchop.lib.common.function.exception.EConsumer;
import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.internal.referencing.AxisDirections;
import org.apache.sis.parameter.DefaultParameterValueGroup;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.transform.AbstractMathTransform;
import org.apache.sis.referencing.operation.transform.DomainDefinition;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.text.ParseException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
@Getter
public final class SISProjectionWrapper implements GeographicProjection {
    private static boolean canInvokeNoArgsConstructor() {
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            if (AdvancedEarthGui.class.getName().equals(element.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private final CoordinateReferenceSystem geoCRS = TPP_GEO_CRS;
    private final CoordinateReferenceSystem projectedCRS;

    private final MathTransform toGeo;
    private final MathTransform fromGeo;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SISProjectionWrapper(
            @JsonProperty(value = "standard") WKTStandard standard,
            @JsonProperty(value = "crs") String crs) throws ParseException {
        this((standard != null && crs != null) || !canInvokeNoArgsConstructor()
                ? (CoordinateReferenceSystem) Objects.requireNonNull(standard, "missing required creator property 'standard'").parse(Objects.requireNonNull(crs, "missing required creator property 'crs'"))
                : TPP_GEO_CRS);
    }

    public SISProjectionWrapper(@NonNull CoordinateReferenceSystem projectedCRS) {
        this.projectedCRS = projectedCRS;

        this.toGeo = SISHelper.findOperation(this.projectedCRS, this.geoCRS).getMathTransform();
        this.fromGeo = SISHelper.findOperation(this.geoCRS, this.projectedCRS).getMathTransform();
    }

    @JsonGetter("standard")
    private WKTStandard standard() {
        return WKTStandard.WKT2_2015;
    }

    @JsonGetter("crs")
    public String getProjectedCRSAsWKT() {
        return this.standard().format(this.projectedCRS);
    }

    @Override
    @SneakyThrows(TransformException.class)
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        double[] point = { x, y };
        this.toGeo.transform(point, 0, point, 0, 1);
        return point;
    }

    @Override
    @SneakyThrows(TransformException.class)
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] point = { longitude, latitude };
        this.fromGeo.transform(point, 0, point, 0, 1);
        return point;
    }

    @Override
    @SneakyThrows(TransformException.class)
    public Matrix2 toGeoDerivative(double x, double y) throws OutOfProjectionBoundsException {
        return Matrix2.castOrCopy(this.toGeo.derivative(new DirectPosition2D(x, y)));
    }

    @Override
    @SneakyThrows(TransformException.class)
    public Matrix2 fromGeoDerivative(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return Matrix2.castOrCopy(this.fromGeo.derivative(new DirectPosition2D(longitude, latitude)));
    }

    private Optional<double[]> tryExtractBoundsFromAxes(@NonNull CoordinateSystem cs, boolean allowInfinity) {
        checkArg(cs.getDimension() == 2);

        CoordinateSystemAxis longitudeAxis = cs.getAxis(0);
        CoordinateSystemAxis latitudeAxis = cs.getAxis(1);

        if (!allowInfinity && (Double.isInfinite(longitudeAxis.getMinimumValue()) || Double.isInfinite(longitudeAxis.getMaximumValue())
                               || Double.isInfinite(latitudeAxis.getMinimumValue()) || Double.isInfinite(latitudeAxis.getMaximumValue()))) {
            return Optional.empty();
        }

        if (AxisDirections.absolute(longitudeAxis.getDirection()) == AxisDirection.NORTH) {
            CoordinateSystemAxis tmp = longitudeAxis;
            longitudeAxis = latitudeAxis;
            latitudeAxis = tmp;
        }

        if (AxisDirections.absolute(longitudeAxis.getDirection()) != AxisDirection.EAST
            || AxisDirections.absolute(latitudeAxis.getDirection()) != AxisDirection.NORTH) {
            return Optional.empty();
        }

        return Optional.of(new double[]{
                longitudeAxis.getMinimumValue(), latitudeAxis.getMinimumValue(),
                longitudeAxis.getMaximumValue(), latitudeAxis.getMaximumValue(),
        });
    }

    @Override
    public double[] boundsGeo() {
        Extent extent = this.projectedCRS.getDomainOfValidity();
        if (extent != null) {
            Collection<? extends GeographicExtent> geographicExtents = extent.getGeographicElements();
            checkState(geographicExtents.size() == 1, "unexpected number of geographic extents: '%s'", geographicExtents.size());

            for (GeographicExtent geographicExtent : geographicExtents) {
                GeographicBoundingBox boundingBox = (GeographicBoundingBox) geographicExtent;
                return new double[]{
                        boundingBox.getWestBoundLongitude(),
                        boundingBox.getSouthBoundLatitude(),
                        boundingBox.getEastBoundLongitude(),
                        boundingBox.getNorthBoundLatitude(),
                };
            }
        }

        return this.tryExtractBoundsFromAxes(this.geoCRS.getCoordinateSystem(), false).orElseGet(GeographicProjection.super::boundsGeo);
    }

    @Override
    @SneakyThrows(TransformException.class)
    public double[] bounds() { //TODO: remove or fix this
        GeneralEnvelope initialGeoEnvelope = new GeneralEnvelope(this.geoCRS);
        initialGeoEnvelope.setEnvelope(this.boundsGeo());

        DomainDefinition geoDomainDefinition = new DomainDefinition();
        geoDomainDefinition.intersect(initialGeoEnvelope);

        DomainDefinition projDomainDefinition = new DomainDefinition();

        if (this.fromGeo instanceof AbstractMathTransform) {
            ((AbstractMathTransform) this.fromGeo).getDomain(geoDomainDefinition)
                    .ifPresent((EConsumer<Envelope>) geoEnvelope -> projDomainDefinition.intersect(Envelopes.transform(this.fromGeo, geoEnvelope)));
        }

        if (this.toGeo instanceof AbstractMathTransform) {
            Envelope envelope = ((AbstractMathTransform) this.toGeo).getDomain(projDomainDefinition).orElse(null);
            if (envelope != null) {
                return new double[]{
                        envelope.getMinimum(0),
                        envelope.getMinimum(1),
                        envelope.getMaximum(0),
                        envelope.getMaximum(1),
                };
            }
        }

        Extent extent = this.projectedCRS.getDomainOfValidity();
        if (extent != null) {
            Collection<? extends GeographicExtent> geographicExtents = extent.getGeographicElements();
            checkState(geographicExtents.size() == 1, "unexpected number of geographic extents: '%s'", geographicExtents.size());

            for (GeographicExtent geographicExtent : geographicExtents) {
                Envelope envelope = Envelopes.transform(this.fromGeo, new GeneralEnvelope((GeographicBoundingBox) geographicExtent));
                return new double[]{
                        envelope.getMinimum(0),
                        envelope.getMinimum(1),
                        envelope.getMaximum(0),
                        envelope.getMaximum(1),
                };
            }
        }

        try { //TODO: this is pretty gross
            double[] boundsGeo = this.boundsGeo();

            //get max in by using extreme coordinates
            double[] bounds = new double[4];

            System.arraycopy(this.fromGeo(boundsGeo[0], boundsGeo[1]), 0, bounds, 0, 2);
            System.arraycopy(this.fromGeo(boundsGeo[2], boundsGeo[3]), 0, bounds, 2, 2);

            if (bounds[0] > bounds[2]) {
                double t = bounds[0];
                bounds[0] = bounds[2];
                bounds[2] = t;
            }

            if (bounds[1] > bounds[3]) {
                double t = bounds[1];
                bounds[1] = bounds[3];
                bounds[3] = t;
            }

            return bounds;
        } catch (OutOfProjectionBoundsException e) {
            throw new IllegalStateException(this.toString());
        }
    }

    @Override
    public ParameterValueGroup parameters() {
        ParameterDescriptor<WKTStandard> standardParameter = new ParameterBuilder().setRequired(true)
                .addName("standard")
                .createEnumerated(WKTStandard.class, WKTStandard.values(), WKTStandard.WKT2_2015);

        ParameterDescriptor<String> crsParameter = new ParameterBuilder().setRequired(true)
                .addName("crs")
                .create(String.class, this.standard().format(TPP_GEO_CRS));

        ParameterDescriptorGroup descriptors = new ParameterBuilder().addName("wkt").createGroup(standardParameter, crsParameter);
        DefaultParameterValueGroup parameters = new DefaultParameterValueGroup(descriptors);

        parameters.getOrCreate(standardParameter).setValue(this.standard());
        parameters.getOrCreate(crsParameter).setValue(this.getProjectedCRSAsWKT());

        return parameters;
    }
}
