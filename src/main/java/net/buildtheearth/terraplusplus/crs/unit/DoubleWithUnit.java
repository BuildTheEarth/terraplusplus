package net.buildtheearth.terraplusplus.crs.unit;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.With;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * A scalar {@code double} representing a value in a specific {@link Unit unit of measurement}.
 *
 * @author DaPorkchop_
 */
@Data
@With(AccessLevel.PRIVATE)
public final class DoubleWithUnit implements Internable<DoubleWithUnit> {
    private final double value;

    @NonNull
    private final Unit unit;

    /**
     * Converts the value to the given target {@link Unit} and returns the result.
     *
     * @param targetUnit the {@link Unit} to convert the value to
     * @return the value converted to the given target {@link Unit}
     */
    public double value(@NonNull Unit targetUnit) {
        if (targetUnit == this.unit) { //fast-track for the common case where units are identity equal
            return this.value;
        }

        return this.unit.convertTo(targetUnit).convert(this.value);
    }

    @Override
    public DoubleWithUnit intern() {
        return InternHelper.intern(this.withUnit(this.unit.intern()));
    }
}
