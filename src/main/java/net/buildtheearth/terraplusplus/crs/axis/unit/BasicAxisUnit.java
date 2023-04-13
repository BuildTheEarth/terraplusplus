package net.buildtheearth.terraplusplus.crs.axis.unit;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.buildtheearth.terraplusplus.crs.axis.unit.conversion.AxisUnitConverterIdentity;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.daporkchop.lib.common.util.PorkUtil;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public final class BasicAxisUnit implements AxisUnit {
    public static BasicAxisUnit makeBase(@NonNull UnitType type) {
        return new BasicAxisUnit(type, null, null, null, null);
    }

    public static BasicAxisUnit makeBase(@NonNull UnitType type, String name, String symbol) {
        return new BasicAxisUnit(type, null, null, name, symbol);
    }

    @NonNull
    private final UnitType type;

    private final AxisUnit baseUnit;
    @Getter(AccessLevel.NONE)
    private final AxisUnitConverter toBaseConverter; //non-null iff (this.baseUnit != null)

    @With
    private final String name;
    @With
    private final String symbol;

    @Override
    public AxisUnitConverter convertTo(@NonNull AxisUnit target) {
        checkArg(this.type == target.type(), "can't convert from %s to %s: mismatched unit types!", this, target);

        AxisUnit targetBaseUnit = target.baseUnit();
        if (this.baseUnit != null) {
            if (targetBaseUnit != null) {
                //both units are derived from a base unit - we can only convert between them if they're both derived from the same base
                checkArg(this.baseUnit.equals(target.baseUnit()), "can't convert from %s to %s: mismatched base units!", this, target);

                //convert to the base unit, and from there to the target unit
                return this.toBaseConverter.andThen(targetBaseUnit.convertTo(target));
            } else {
                //this unit is derived from another unit, but the target unit is a base unit - we can only convert between them if the target unit is this unit's base
                checkArg(this.baseUnit.equals(target), "can't convert from %s to %s: mismatched base units!", this, target);

                //convert from this unit to the base unit (which is already the target unit)
                return this.toBaseConverter;
            }
        } else {
            if (targetBaseUnit != null) {
                //the target unit is derived from another unit, but this unit is a base unit - we can only convert between them if this unit is the target unit's base
                checkArg(this.equals(targetBaseUnit), "can't convert from %s to %s: mismatched base units!", this, target);

                //convert from the target unit to the base unit (which is already the current unit), then use the inverse of that conversion
                return target.convertTo(this).inverse();
            } else {
                //neither unit is derived from another unit, meaning they are both base units - we can only convert between them if they're both the same
                checkArg(this.equals(target), "can't convert from %s to %s: mismatched base units!", this, target);

                return AxisUnitConverterIdentity.instance();
            }
        }
    }

    @Override
    public AxisUnit transform(@NonNull AxisUnitConverter converter) {
        return new BasicAxisUnit(this.type,
                PorkUtil.fallbackIfNull(this.baseUnit, this),
                this.toBaseConverter != null ? converter.andThen(this.toBaseConverter) : converter,
                null, null);
    }

    @Override
    public AxisUnit intern() {
        AxisUnit baseUnit = InternHelper.tryInternNullable(this.baseUnit);
        AxisUnitConverter toBaseConverter = InternHelper.tryInternNullable(this.toBaseConverter);
        String name = InternHelper.tryInternNullable(this.name);
        String symbol = InternHelper.tryInternNullable(this.symbol);

        //noinspection StringEquality
        return InternHelper.intern(baseUnit != this.baseUnit || toBaseConverter != this.toBaseConverter || name != this.name || symbol != this.symbol
                ? new BasicAxisUnit(this.type, baseUnit, toBaseConverter, name, symbol)
                : this);
    }
}
