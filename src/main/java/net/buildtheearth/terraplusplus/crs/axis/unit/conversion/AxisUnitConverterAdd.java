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
public final class AxisUnitConverterAdd extends AbstractAxisUnitConverter {
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
    protected AxisUnitConverter inverse0() {
        return new AxisUnitConverterAdd(-this.offset);
    }

    @Override
    protected AxisUnitConverter simplify(boolean intern) {
        return this;
    }

    @Override
    protected AxisUnitConverter tryAndThen(@NonNull AxisUnitConverter next) {
        if (next instanceof AxisUnitConverterAdd) {
            return new AxisUnitConverterAdd(this.offset + ((AxisUnitConverterAdd) next).offset);
        }

        return super.tryAndThen(next);
    }
}
