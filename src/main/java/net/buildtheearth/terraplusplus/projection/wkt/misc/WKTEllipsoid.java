package net.buildtheearth.terraplusplus.projection.wkt.misc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.AbstractWKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTValueInMetreOrValueAndUnit;

import java.io.IOException;
import java.util.Objects;

import static net.buildtheearth.terraplusplus.util.TerraUtils.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#52">WKT Specification §8.2.1: Geodetic datum - Ellipsoid</a>
 */
@JsonIgnoreProperties("$schema")
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public final class WKTEllipsoid extends AbstractWKTObject.WithID implements AbstractWKTObject.AutoDeserialize {
    @NonNull
    private final String name;

    @Builder.Default
    @JsonProperty("semi_major_axis")
    private final Number semiMajorAxis = null; // a

    /**
     * Exactly one of this field and {@link #inverseFlattening} is non-{@code null}.
     */
    @Builder.Default
    @JsonProperty("semi_minor_axis")
    private final Number semiMinorAxis = null; // b

    /**
     * May be {@code 0.0d}, representing a value of infinity (in which case the ellipsoid is a sphere).
     * <p>
     * Exactly one of this field and {@link #semiMinorAxis} is non-{@code null}.
     * <p>
     * Equivalent to {@code semiMajorAxis / (semiMajorAxis - semiMinorAxis)}.
     */
    @Builder.Default
    @JsonProperty("inverse_flattening")
    private final Number inverseFlattening = null; //  1 / f = a / (a - b)

    /**
     * The length unit for {@link #semiMajorAxis} (and {@link #semiMinorAxis}, if present).
     */
    @Builder.Default
    @JsonIgnore
    private final WKTLengthUnit unit = null;

    public boolean isSphere() {
        if (this.inverseFlattening() != null) {
            //TODO: maybe also check if the inverse flattening is infinity?
            return numbersEqual(this.inverseFlattening(), 0.0d);
        } else { //semiMinorAxis must be non-null
            return numbersEqual(this.semiMajorAxis(), this.semiMinorAxis());
        }
    }

    // b = a - a * f = a - a * (1 / (1 / f))
    public double getOrComputeSemiMinorAxis() {
        if (this.inverseFlattening() != null) {
            double semiMajorAxis = toDoubleExact(this.semiMajorAxis());
            double inverseFlattening = toDoubleExact(this.inverseFlattening());
            // a - a * (1 / (1 / f)) = a * (1 - (1 / (1 / f)))
            //TODO: this could be alternatively be implemented at high precision using FMA
            return semiMajorAxis * (1.0d - (1.0d / inverseFlattening));
        } else {
            return toDoubleExact(this.semiMinorAxis());
        }
    }

    // 1 / f = a / (a - b)
    public double getOrComputeInverseFlattening() {
        if (this.inverseFlattening() != null) {
            return toDoubleExact(this.inverseFlattening());
        } else {
            double semiMajorAxis = toDoubleExact(this.semiMajorAxis());
            double semiMinorAxis = toDoubleExact(this.semiMinorAxis());
            return semiMajorAxis / (semiMajorAxis - semiMinorAxis);
        }
    }

    // 1 / (1 / f) = (a - b) / a
    public double computeFlattening() {
        if (this.inverseFlattening() != null) {
            return 1.0d / toDoubleExact(this.inverseFlattening());
        } else {
            double semiMajorAxis = toDoubleExact(this.semiMajorAxis());
            double semiMinorAxis = toDoubleExact(this.semiMinorAxis());
            return (semiMajorAxis - semiMinorAxis) / semiMajorAxis;
        }
    }

    // e ^ 2 = 2 * f - f ^ 2
    public double computeEccentricitySquared() {
        if (this.inverseFlattening() != null) {
            // 2 * f - f ^ 2 = (2 * (1 / f) - 1) / ((1 / f) ^ 2)
            double inverseFlattening = toDoubleExact(this.inverseFlattening());
            return (2.0d * inverseFlattening - 1.0d) / (inverseFlattening * inverseFlattening);
        } else {
            // 2 * f - f ^ 2 = 1 - ((b ^ 2) / (a ^ 2))
            double semiMajorAxis = toDoubleExact(this.semiMajorAxis());
            double semiMinorAxis = toDoubleExact(this.semiMinorAxis());
            return 1.0d - ((semiMajorAxis * semiMajorAxis) / (semiMinorAxis * semiMinorAxis));
        }
    }

    // e = sqrt(e ^ 2)
    public double computeEccentricity() {
        return Math.sqrt(this.computeEccentricitySquared());
    }

    // e' ^ 2 = (e ^ 2) / (1 - e ^ 2)
    public double computeSecondEccentricitySquared() {
        double eccentricitySquared = this.computeEccentricitySquared();
        return eccentricitySquared / (1.0d - eccentricitySquared);
    }

    // e' = sqrt(e' ^ 2)
    public double computeSecondEccentricity() {
        return Math.sqrt(this.computeSecondEccentricitySquared());
    }

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ELLIPSOID")
                .writeQuotedLatinString(this.name)
                .writeUnsignedNumericLiteral(this.semiMajorAxis())
                .writeUnsignedNumericLiteral(this.getOrComputeInverseFlattening())
                .writeOptionalObject(this.unit())
                .writeOptionalObject(this.id())
                .endObject();
    }

    static final class WKTEllipsoidBuilderImpl extends WKTEllipsoidBuilder<WKTEllipsoid, WKTEllipsoidBuilderImpl> {
        @Override
        public WKTEllipsoid build() {
            WKTEllipsoid ellipsoid = new WKTEllipsoid(this);
            checkState(ellipsoid.semiMinorAxis() != null ^ ellipsoid.inverseFlattening() != null, "exactly one of inverseFlattening or semiMinorAxis or radius must be set!");
            return ellipsoid;
        }
    }

    public static abstract class WKTEllipsoidBuilder<C extends WKTEllipsoid, B extends WKTEllipsoidBuilder<C, B>> extends WithIDBuilder<C, B> {
        @JsonIgnore
        public B unit(WKTLengthUnit unit) {
            checkState(!this.unit$set || Objects.equals(this.unit$value, unit), "ellipsoid contains inconsistent units! %s != %s", this.unit$value, unit);
            this.unit$value = unit;
            this.unit$set = true;
            return this.self();
        }

        @JsonIgnore
        public B semiMajorAxis(@NonNull Number semiMajorAxis) {
            this.semiMajorAxis$value = semiMajorAxis;
            this.semiMajorAxis$set = true;
            return this.self();
        }

        @JsonProperty("semi_major_axis")
        public B semiMajorAxis(@NonNull WKTValueInMetreOrValueAndUnit semiMajorAxis) {
            checkState(!this.semiMajorAxis$set, "semiMajorAxis has already been set!");
            return this.unit(semiMajorAxis.unit()).semiMajorAxis(semiMajorAxis.value());
        }

        @JsonIgnore
        public B semiMinorAxis(@NonNull Number semiMinorAxis) {
            this.semiMinorAxis$value = semiMinorAxis;
            this.semiMinorAxis$set = true;
            return this.self();
        }

        @JsonProperty("semi_minor_axis")
        public B semiMinorAxis(@NonNull WKTValueInMetreOrValueAndUnit semiMinorAxis) {
            checkState(!this.semiMinorAxis$set, "semiMinorAxis has already been set!");
            checkState(!this.inverseFlattening$set, "cannot set semiMinorAxis when inverseFlattening has already been set!");
            return this.unit(semiMinorAxis.unit()).semiMinorAxis(semiMinorAxis.value());
        }

        public B radius(@NonNull WKTValueInMetreOrValueAndUnit radius) {
            return this.semiMajorAxis(radius).inverseFlattening(0.0d);
        }
    }
}
