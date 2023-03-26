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
import net.buildtheearth.terraplusplus.projection.wkt.WKTParseSchema;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;

import java.io.IOException;

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
    public static final WKTParseSchema<WKTEllipsoid> PARSE_SCHEMA = WKTParseSchema.builder(WKTEllipsoidBuilderImpl::new, WKTEllipsoidBuilder::build)
            .permitKeyword("ELLIPSOID", "SPHEROID")
            .requiredStringProperty(WKTEllipsoidBuilder::name)
            .requiredUnsignedNumericAsDoubleProperty(WKTEllipsoidBuilder::semiMajorAxis)
            .requiredUnsignedNumericAsDoubleProperty(WKTEllipsoidBuilder::inverseFlattening)
            .optionalObjectProperty(WKTLengthUnit.PARSE_SCHEMA, WKTEllipsoidBuilder::lengthUnit)
            .inheritFrom(BASE_PARSE_SCHEMA)
            .build();

    @NonNull
    private final String name;

    @JsonProperty("semi_major_axis")
    private final double semiMajorAxis;

    /**
     * May be {@code 0.0d}, representing a value of infinity (in which case the ellipsoid is a sphere).
     */
    @JsonProperty("inverse_flattening")
    private final double inverseFlattening;

    @Builder.Default
    @JsonProperty("length_unit")
    private final WKTLengthUnit lengthUnit = null;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ELLIPSOID")
                .writeQuotedLatinString(this.name)
                .writeUnsignedNumericLiteral(this.semiMajorAxis)
                .writeUnsignedNumericLiteral(this.inverseFlattening)
                .writeOptionalObject(this.lengthUnit)
                .writeOptionalObject(this.id())
                .endObject();
    }
}
