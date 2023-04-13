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
public final class UnitConverterMultiply extends AbstractUnitConverter {
    private final double factor;

    @Override
    public boolean isIdentity() {
        return this.factor == 1.0d;
    }

    @Override
    public double convert(double value) {
        return value * this.factor;
    }

    @Override
    protected UnitConverter inverse0() {
        return new UnitConverterMultiply(1.0d / this.factor);
    }

    @Override
    protected UnitConverter simplify0() {
        return this;
    }

    @Override
    protected UnitConverter tryAndThen(@NonNull UnitConverter next) {
        if (next instanceof UnitConverterMultiply) {
            return new UnitConverterMultiply(this.factor * ((UnitConverterMultiply) next).factor);
        }

        return super.tryAndThen(next);
    }
}
