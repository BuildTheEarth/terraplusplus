package net.buildtheearth.terraplusplus.crs.axis.unit;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.axis.unit.conversion.AxisUnitConverterAdd;
import net.buildtheearth.terraplusplus.crs.axis.unit.conversion.AxisUnitConverterMultiply;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * @author DaPorkchop_
 */
public interface AxisUnit extends Internable<AxisUnit> {
    /**
     * @return the type of value measured by this unit
     */
    UnitType type();

    /**
     * @return the unscaled unit which this unit is derived from
     */
    AxisUnit baseUnit();

    /**
     * @return this unit's name, or {@code null} if this unit does not have a name itself (e.g. derived units without an explicit name)
     */
    String name();

    /**
     * @return this unit's symbol, or {@code null} if this unit does not have a symbol itself (e.g. derived units without an explicitly defined symbol)
     */
    String symbol();

    /**
     * Gets an {@link AxisUnitConverter} which can convert values measured in this unit to the given target unit.
     *
     * @param target the {@link AxisUnit} to convert to
     * @return an {@link AxisUnitConverter} which can convert values measured in this unit to the given target unit
     * @throws IllegalArgumentException if the given {@code target} unit is of a different {@link #type()} than this unit
     */
    AxisUnitConverter convertTo(@NonNull AxisUnit target);

    default AxisUnit add(double offset) {
        return this.transform(new AxisUnitConverterAdd(offset));
    }

    default AxisUnit multiply(double factor) {
        return this.transform(new AxisUnitConverterMultiply(factor));
    }

    AxisUnit transform(@NonNull AxisUnitConverter converter);

    AxisUnit withName(String name);

    AxisUnit withSymbol(String symbol);
}
