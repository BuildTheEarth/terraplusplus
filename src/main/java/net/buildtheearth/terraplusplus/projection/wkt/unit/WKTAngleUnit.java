package net.buildtheearth.terraplusplus.projection.wkt.unit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#35">WKT Specification §7.4: Unit and unit conversion factor</a>
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@RequiredArgsConstructor
@Data
public final class WKTAngleUnit extends WKTObject {
    @NonNull
    private final String name;

    /**
     * The number of radians per unit.
     */
    private final double conversionFactor;

    @Override
    public String toString() {
        return "ANGLEUNIT[\"" + this.name.replace("\"", "\"\"") + "\", " + this.conversionFactor + (this.id() != null ? ", " + this.id() : "") + ']';
    }
}
