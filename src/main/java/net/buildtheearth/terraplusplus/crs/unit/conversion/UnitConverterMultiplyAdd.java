package net.buildtheearth.terraplusplus.crs.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.buildtheearth.terraplusplus.crs.unit.UnitConverter;

/**
 * @author DaPorkchop_
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class UnitConverterMultiplyAdd extends AbstractUnitConverter implements AbstractUnitConverter.RepresentableAsSequence {
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
    public UnitConverterSequence asConverterSequence() {
        return new UnitConverterSequence(ImmutableList.of(
                new UnitConverterMultiply(this.factor),
                new UnitConverterAdd(this.offset)
        ));
    }

    @Override
    protected UnitConverter inverse0() {
        return this.asConverterSequence().inverse();
    }

    @Override
    protected UnitConverter simplify0() {
        //this is already maximally simplified
        return this;
    }
}
