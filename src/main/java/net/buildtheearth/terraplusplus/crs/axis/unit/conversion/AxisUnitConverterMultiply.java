package net.buildtheearth.terraplusplus.crs.axis.unit.conversion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnitConverter;

/**
 * @author DaPorkchop_
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class AxisUnitConverterMultiply extends AbstractAxisUnitConverter {
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
    protected AxisUnitConverter inverse0() {
        return new AxisUnitConverterMultiply(1.0d / this.factor);
    }

    @Override
    protected AxisUnitConverter simplify(boolean intern) {
        return this;
    }

    @Override
    protected AxisUnitConverter tryAndThen(@NonNull AxisUnitConverter next) {
        if (next instanceof AxisUnitConverterMultiply) {
            return new AxisUnitConverterMultiply(this.factor * ((AxisUnitConverterMultiply) next).factor);
        }

        return super.tryAndThen(next);
    }
}
