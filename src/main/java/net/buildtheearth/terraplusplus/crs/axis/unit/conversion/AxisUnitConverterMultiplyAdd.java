package net.buildtheearth.terraplusplus.crs.axis.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnitConverter;

/**
 * @author DaPorkchop_
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class AxisUnitConverterMultiplyAdd extends AbstractAxisUnitConverter implements AbstractAxisUnitConverter.RepresentableAsSequence {
    private final double factor;
    private final double offset;

    @Override
    public boolean isIdentity() {
        return this.factor == 1.0d && this.offset == 0.0d;
    }

    @Override
    public double convert(double value) {
        return value * this.factor + this.offset;
    }

    @Override
    public AxisUnitConverterSequence asConverterSequence() {
        return new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterMultiply(this.factor),
                new AxisUnitConverterAdd(this.offset)
        ));
    }

    @Override
    protected AxisUnitConverter inverse0() {
        return this.asConverterSequence().inverse();
    }

    @Override
    protected AxisUnitConverter simplify0() {
        //this is already maximally simplified
        return this;
    }
}
