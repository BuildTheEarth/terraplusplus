package net.buildtheearth.terraplusplus.crs.unit.conversion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.unit.UnitConverter;

/**
 * @author DaPorkchop_
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class UnitConverterAdd extends AbstractUnitConverter {
    private final double offset;

    @Override
    public boolean isIdentity() {
        return this.offset == 0.0d;
    }

    @Override
    public double convert(double value) {
        return value + this.offset;
    }

    @Override
    protected UnitConverter inverse0() {
        return new UnitConverterAdd(-this.offset);
    }

    @Override
    protected UnitConverter simplify0() {
        return this;
    }

    @Override
    protected UnitConverter tryAndThen(@NonNull UnitConverter next) {
        if (next instanceof UnitConverterAdd) {
            return new UnitConverterAdd(this.offset + ((UnitConverterAdd) next).offset);
        }

        return super.tryAndThen(next);
    }
}
