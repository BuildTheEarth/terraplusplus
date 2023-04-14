package net.buildtheearth.terraplusplus.crs.unit;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.buildtheearth.terraplusplus.crs.unit.conversion.UnitConverterIdentity;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.daporkchop.lib.common.util.PorkUtil;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
public final class BasicUnit implements Unit {
    public static BasicUnit makeBase(@NonNull UnitType type) {
        return new BasicUnit(type, null, null, null, null);
    }

    public static BasicUnit makeBase(@NonNull UnitType type, String name, String symbol) {
        return new BasicUnit(type, null, null, name, symbol);
    }

    @NonNull
    private final UnitType type;

    private final Unit baseUnit;
    @Getter(AccessLevel.NONE)
    private final UnitConverter toBaseConverter; //non-null iff (this.baseUnit != null)

    @With
    private final String name;
    @With
    private final String symbol;

    @Override
    public UnitConverter convertTo(@NonNull Unit target) {
        checkArg(this.type == target.type(), "can't convert from %s to %s: mismatched unit types!", this, target);

        Unit targetBaseUnit = target.baseUnit();
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

                return UnitConverterIdentity.instance();
            }
        }
    }

    @Override
    public boolean compatibleWith(@NonNull Unit other) {
        if (this.equals(other)) {
            return true;
        }

        return this.type.equals(other.type())
               && PorkUtil.fallbackIfNull(this.baseUnit, this).equals(PorkUtil.fallbackIfNull(other.baseUnit(), other));
    }

    @Override
    public Unit transform(@NonNull UnitConverter converter) {
        if (converter.isIdentity()) {
            return this;
        }

        return new BasicUnit(this.type,
                PorkUtil.fallbackIfNull(this.baseUnit, this),
                this.toBaseConverter != null ? converter.andThen(this.toBaseConverter) : converter,
                null, null);
    }

    @Override
    public Unit intern() {
        Unit baseUnit = InternHelper.tryInternNullableInternable(this.baseUnit);
        UnitConverter toBaseConverter = InternHelper.tryInternNullableInternable(this.toBaseConverter);
        String name = InternHelper.tryInternNullableString(this.name);
        String symbol = InternHelper.tryInternNullableString(this.symbol);

        //noinspection StringEquality
        return InternHelper.intern(baseUnit != this.baseUnit || toBaseConverter != this.toBaseConverter || name != this.name || symbol != this.symbol
                ? new BasicUnit(this.type, baseUnit, toBaseConverter, name, symbol)
                : this);
    }
}
