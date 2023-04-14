package net.buildtheearth.terraplusplus.crs.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.unit.UnitConverter;
import net.buildtheearth.terraplusplus.util.TerraUtils;

/**
 * @author DaPorkchop_
 */
@Data
@EqualsAndHashCode(callSuper = false, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@SuppressWarnings("UnstableApiUsage")
public final class UnitConverterSequence extends AbstractUnitConverter implements AbstractUnitConverter.RepresentableAsSequence {
    @NonNull
    private final ImmutableList<UnitConverter> converters;

    @Override
    public boolean isIdentity() {
        return this.converters.stream().allMatch(UnitConverter::isIdentity);
    }

    @Override
    public double convert(double value) {
        for (int i = 0; i < this.converters.size(); i++) {
            value = this.converters.get(i).convert(value);
        }
        return value;
    }

    @Override
    protected UnitConverter inverse0() {
        return new UnitConverterSequence(this.converters.reverse().stream().map(UnitConverter::inverse).collect(ImmutableList.toImmutableList()));
    }

    @Override
    public UnitConverterSequence asConverterSequence() {
        return this;
    }

    @Override
    protected UnitConverter withChildrenInterned() {
        ImmutableList<UnitConverter> converters = this.converters;
        ImmutableList<UnitConverter> internedConverters = TerraUtils.internElements(converters);
        return converters == internedConverters ? this : new UnitConverterSequence(internedConverters);
    }

    private static boolean isInverseFactors(double a, double b) {
        //check both ways to account for floating-point error
        return a == 1.0d / b || 1.0d / a == b;
    }

    @Override
    protected UnitConverter simplify0() {
        ImmutableList<UnitConverter> converters = this.converters;
        ImmutableList<UnitConverter> prevConverters;

        //this loop will keep running until no more optimizations can be made
        boolean flattened = false;
        do {
            prevConverters = converters;

            //special handling for special converter counts
            switch (converters.size()) {
                case 0:
                    return UnitConverterIdentity.instance();
                case 1:
                    return converters.get(0).simplify();
            }

            //flatten nested converter sequences
            // (we only want to do this once, as none of the other transformations in this loop can actually cause any sequence converters
            // to be inserted into the sequence)
            if (!flattened) {
                flattened = true;
                converters = TerraUtils.maybeFlatten(converters, converter -> converter instanceof RepresentableAsSequence
                        ? ((RepresentableAsSequence) converter).asConverterSequence().converters()
                        : null);
            }

            //simplify the converters
            converters = TerraUtils.maybeRemap(converters, UnitConverter::simplify);

            //remove identity converters
            converters = TerraUtils.maybeRemove(converters, UnitConverter::isIdentity);

            //try to merge neighboring converters
            converters = TerraUtils.maybeMerge2Neighbors(converters, (first, second) -> {
                if (first.getClass() == second.getClass()) { //both are the same type
                    if (first instanceof UnitConverterAdd) {
                        return new UnitConverterAdd(((UnitConverterAdd) first).offset() + ((UnitConverterAdd) second).offset());
                    } else if (first instanceof UnitConverterMultiply) {
                        double firstFactor = ((UnitConverterMultiply) first).factor();
                        double secondFactor = ((UnitConverterMultiply) second).factor();
                        if (isInverseFactors(firstFactor, secondFactor)) { //one is the inverse of the other
                            return UnitConverterIdentity.instance();
                        } else {
                            return new UnitConverterMultiply(firstFactor * secondFactor);
                        }
                    }
                }

                return null;
            });

            converters = TerraUtils.maybeMerge3Neighbors(converters, (a, b, c) -> {
                if (a instanceof UnitConverterMultiply && b instanceof UnitConverterAdd && c instanceof UnitConverterMultiply) {
                    double aFactor = ((UnitConverterMultiply) a).factor();
                    double bOffset = ((UnitConverterAdd) b).offset();
                    double cFactor = ((UnitConverterMultiply) c).factor();

                    if (isInverseFactors(aFactor, cFactor)) {
                        return ImmutableList.of(new UnitConverterAdd(1.0d / aFactor == cFactor
                                ? bOffset / aFactor // (value * a + b) / a = value + (b / a)
                                : bOffset * cFactor // ((value / c) + b) * c = value + b * c
                        ));
                    } else { // (value * a + b) * c = value * a * c + b * c
                        return ImmutableList.of(new UnitConverterMultiply(aFactor * cFactor), new UnitConverterAdd(bOffset * cFactor));
                    }
                } else if (a instanceof UnitConverterAdd && b instanceof UnitConverterMultiply && c instanceof UnitConverterAdd) {
                    double aOffset = ((UnitConverterAdd) a).offset();
                    double bFactor = ((UnitConverterMultiply) b).factor();
                    double cOffset = ((UnitConverterAdd) c).offset();

                    // ((value + a) * b) + c = value * b + (a * b + c)
                    return ImmutableList.of(new UnitConverterMultiply(bFactor), new UnitConverterAdd(aOffset * bFactor + cOffset));
                }

                return null;
            });
        } while (converters != prevConverters);

        switch (converters.size()) {
            case 0:
            case 1:
                throw new IllegalStateException();
            case 2:
                //special cases with where there are two converters
                UnitConverter a = converters.get(0);
                UnitConverter b = converters.get(1);
                if (a instanceof UnitConverterMultiply && b instanceof UnitConverterAdd) { // [multiply, add] -> multiply and add
                    double aFactor = ((UnitConverterMultiply) a).factor();
                    double bOffset = ((UnitConverterAdd) b).offset();

                    return new UnitConverterMultiplyAdd(aFactor, bOffset);
                } else if (a instanceof UnitConverterAdd && b instanceof UnitConverterMultiply) { // [add, multiply] -> multiply and add
                    double aOffset = ((UnitConverterAdd) a).offset();
                    double bFactor = ((UnitConverterMultiply) b).factor();

                    // (value + a) * b = value * b + a * b
                    return new UnitConverterMultiplyAdd(bFactor, aOffset * bFactor);
                }
                break;
        }

        return converters == this.converters ? this : new UnitConverterSequence(converters);
    }

    @Override
    protected UnitConverter tryAndThen(@NonNull UnitConverter next) {
        if (next instanceof UnitConverterSequence) { //concatenate the two sequences, as otherwise we'd end up with a sequence of sequences (which would be stupid)
            return new UnitConverterSequence(TerraUtils.concat(this.converters, ((UnitConverterSequence) next).converters));
        }

        //TODO: do we care about trying to do further simplifications here?

        return super.tryAndThen(next);
    }
}
