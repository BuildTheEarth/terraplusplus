package net.buildtheearth.terraplusplus.crs.unit;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.unit.conversion.UnitConverterAdd;
import net.buildtheearth.terraplusplus.crs.unit.conversion.UnitConverterMultiply;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * @author DaPorkchop_
 */
public interface Unit extends Internable<Unit> {
    Unit METER = BasicUnit.makeBase(UnitType.LENGTH, "meter", "m");

    /**
     * @return the type of value measured by this unit
     */
    UnitType type();

    /**
     * @return the unscaled unit which this unit is derived from
     */
    Unit baseUnit();

    /**
     * @return this unit's name, or {@code null} if this unit does not have a name itself (e.g. derived units without an explicit name)
     */
    String name();

    /**
     * @return this unit's symbol, or {@code null} if this unit does not have a symbol itself (e.g. derived units without an explicitly defined symbol)
     */
    String symbol();

    /**
     * Gets an {@link UnitConverter} which can convert values measured in this unit to the given target unit.
     *
     * @param target the {@link Unit} to convert to
     * @return an {@link UnitConverter} which can convert values measured in this unit to the given target unit
     * @throws IllegalArgumentException if the given {@code target} unit is of a different {@link #type()} than this unit
     */
    UnitConverter convertTo(@NonNull Unit target);

    default Unit add(double offset) {
        return this.transform(new UnitConverterAdd(offset));
    }

    default Unit multiply(double factor) {
        return this.transform(new UnitConverterMultiply(factor));
    }

    Unit transform(@NonNull UnitConverter converter);

    Unit withName(String name);

    Unit withSymbol(String symbol);
}
