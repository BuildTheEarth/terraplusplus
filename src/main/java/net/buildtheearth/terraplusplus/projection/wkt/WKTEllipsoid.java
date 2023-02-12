package net.buildtheearth.terraplusplus.projection.wkt;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.unit.WKTLengthUnit;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#52">WKT Specification §8.2.1: Geodetic datum - Ellipsoid</a>
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@RequiredArgsConstructor
@Data
public final class WKTEllipsoid extends WKTObject {
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
    public String toString() {
        return "ELLIPSOID[\"" + this.name.replace("\"", "\"\"") + "\", " + this.semiMajorAxis + ", " + this.inverseFlattening + ", " + this.lengthUnit
               + (this.id() != null ? ", " + this.id() : "") + ']';
    }
}
