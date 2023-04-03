package net.buildtheearth.terraplusplus.projection.wkt.misc;

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
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTValueInMetreOrValueAndUnit;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

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
    private final WKTValueInMetreOrValueAndUnit semiMajorAxis = null;

    /**
     * May be {@code 0.0d}, representing a value of infinity (in which case the ellipsoid is a sphere).
     * <p>
     * Exactly one of this field and {@link #semiMinorAxis} is non-{@code null}.
     */
    @Builder.Default
    @JsonProperty("inverse_flattening")
    private final WKTValueInMetreOrValueAndUnit inverseFlattening = null;

    /**
     * Exactly one of this field and {@link #inverseFlattening} is non-{@code null}.
     */
    @Builder.Default
    @JsonProperty("semi_minor_axis")
    private final WKTValueInMetreOrValueAndUnit semiMinorAxis = null;

    @Builder.Default
    private final WKTValueInMetreOrValueAndUnit radius = null; //TODO: use this somewhere

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ELLIPSOID")
                .writeQuotedLatinString(this.name)
                .writeUnsignedNumericLiteral(this.semiMajorAxis.value())
                .writeUnsignedNumericLiteral(this.inverseFlattening.value()) //TODO: compute inverse flattening from semi_minor_axis if necessary
                .writeOptionalObject(this.semiMajorAxis.unit())
                .writeOptionalObject(this.id())
                .endObject();
    }

    static final class WKTEllipsoidBuilderImpl extends WKTEllipsoidBuilder<WKTEllipsoid, WKTEllipsoidBuilderImpl> {
        @Override
        public WKTEllipsoid build() {
            WKTEllipsoid ellipsoid = new WKTEllipsoid(this);
            checkState(Stream.of(ellipsoid.inverseFlattening, ellipsoid.semiMinorAxis, ellipsoid.radius).filter(Objects::nonNull).count() == 1,
                    "exactly one of inverseFlattening or semiMinorAxis or radius must be set!");
            return ellipsoid;
        }
    }
}
