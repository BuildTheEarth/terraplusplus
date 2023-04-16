package net.buildtheearth.terraplusplus.projection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.projection.transform.ProjectionTransform;
import net.daporkchop.lib.common.function.throwing.EConsumer;
import net.daporkchop.lib.common.ref.Ref;
import net.daporkchop.lib.common.ref.ThreadRef;
import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.internal.referencing.AxisDirections;
import org.apache.sis.io.wkt.Convention;
import org.apache.sis.io.wkt.FormattableObject;
import org.apache.sis.io.wkt.Formatter;
import org.apache.sis.io.wkt.KeywordCase;
import org.apache.sis.io.wkt.KeywordStyle;
import org.apache.sis.io.wkt.Symbols;
import org.apache.sis.io.wkt.WKTFormat;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.operation.transform.AbstractMathTransform;
import org.apache.sis.referencing.operation.transform.DomainDefinition;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
@Getter
public final class SISProjectionWrapper extends ProjectionTransform {
    private static final Ref<WKTFormat> WKT_FORMAT;

    static {
        WKTFormat format = new WKTFormat(Locale.ROOT, TimeZone.getDefault());
        format.setKeywordCase(KeywordCase.UPPER_CASE);
        format.setKeywordStyle(KeywordStyle.SHORT);
        format.setConvention(Convention.WKT2);
        format.setSymbols(Symbols.SQUARE_BRACKETS);
        format.setIndentation(WKTFormat.SINGLE_LINE);

        WKT_FORMAT = ThreadRef.soft(format::clone);
    }

    @SneakyThrows(ParseException.class)
    private static CoordinateReferenceSystem parseWKT(@NonNull String wkt) {
        return (CoordinateReferenceSystem) WKT_FORMAT.get().parseObject(wkt);
    }

    private final CoordinateReferenceSystem geoCRS = CommonCRS.WGS84.normalizedGeographic();
    private final CoordinateReferenceSystem projectedCRS;

    private final CoordinateOperation toGeo;
    private final CoordinateOperation fromGeo;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SISProjectionWrapper(
            //TODO: this is specifically WKT2:2015 (ISO 19162:2015)
            @JsonProperty(value = "wkt", required = true) @NonNull String wkt) throws FactoryException {
        this(parseWKT(wkt));
    }

    @SneakyThrows(FactoryException.class)
    public SISProjectionWrapper(@NonNull CoordinateReferenceSystem projectedCRS) {
        super(new EquirectangularProjection());

        this.projectedCRS = projectedCRS;

        this.toGeo = CRS.findOperation(this.projectedCRS, this.geoCRS, null);
        this.fromGeo = CRS.findOperation(this.geoCRS, this.projectedCRS, null);
    }

    @JsonGetter("wkt")
    public String getProjectedCRSAsWKT() {
        Formatter formatter = new Formatter(Convention.WKT2, Symbols.SQUARE_BRACKETS, -1);
        formatter.append((FormattableObject) this.projectedCRS);
        return formatter.toWKT();
    }

    @Override
    @SneakyThrows //TODO: proper exception handling
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        DirectPosition geoPosition = this.toGeo.getMathTransform().transform(new DirectPosition2D(x, y), null);
        return new double[]{ geoPosition.getOrdinate(0), geoPosition.getOrdinate(1) };
    }

    @Override
    @SneakyThrows //TODO: proper exception handling
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        DirectPosition projectedPosition = this.fromGeo.getMathTransform().transform(new DirectPosition2D(longitude, latitude), null);
        return new double[]{ projectedPosition.getOrdinate(0), projectedPosition.getOrdinate(1) };
    }

    @Override
    public double metersPerUnit() {
        throw new UnsupportedOperationException();
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

        return Optional.of(new double[] {
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

        return this.tryExtractBoundsFromAxes(this.geoCRS.getCoordinateSystem(), false).orElseGet(super::boundsGeo);
    }

    @Override
    @SneakyThrows(TransformException.class)
    public double[] bounds() { //TODO: remove or fix this
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

        GeneralEnvelope initialGeoEnvelope = new GeneralEnvelope(this.geoCRS);
        initialGeoEnvelope.setEnvelope(this.boundsGeo());

        DomainDefinition geoDomainDefinition = new DomainDefinition();
        geoDomainDefinition.intersect(initialGeoEnvelope);

        DomainDefinition projDomainDefinition = new DomainDefinition();

        if (this.fromGeo.getMathTransform() instanceof AbstractMathTransform) {
            ((AbstractMathTransform) this.fromGeo.getMathTransform()).getDomain(geoDomainDefinition)
                    .ifPresent((EConsumer<Envelope>) geoEnvelope -> projDomainDefinition.intersect(Envelopes.transform(this.fromGeo, geoEnvelope)));
        }

        if (this.toGeo.getMathTransform() instanceof AbstractMathTransform) {
            Envelope envelope = ((AbstractMathTransform) this.toGeo.getMathTransform()).getDomain(projDomainDefinition).orElse(null);
            if (envelope != null) {
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
}
