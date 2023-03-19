package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;

import java.io.IOException;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#52">WKT Specification §8.2.1: Geodetic datum - Ellipsoid</a>
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@RequiredArgsConstructor
@Getter
public final class WKTEllipsoid extends WKTObject {
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

    private final double semiMajorAxis;

    /**
     * May be {@code 0.0d}, representing a value of infinity (in which case the ellipsoid is a sphere).
     */
    private final double inverseFlattening;

    @NonNull
    @Builder.Default
    private final WKTLengthUnit lengthUnit = WKTLengthUnit.METRE;

    @Override
    public void write(@NonNull WKTWriter writer) throws IOException {
        writer.beginObject("ELLIPSOID")
                .writeQuotedLatinString(this.name)
                .writeUnsignedNumericLiteral(this.semiMajorAxis)
                .writeUnsignedNumericLiteral(this.inverseFlattening);
        this.lengthUnit.write(writer);
        if (this.id() != null) {
            this.id().write(writer);
        }
        writer.endObject();
    }
}
