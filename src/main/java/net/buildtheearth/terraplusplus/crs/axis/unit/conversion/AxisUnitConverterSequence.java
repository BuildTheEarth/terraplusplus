package net.buildtheearth.terraplusplus.crs.axis.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnitConverter;
import net.daporkchop.lib.common.function.plain.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author DaPorkchop_
 */
@Data
@EqualsAndHashCode(callSuper = false, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@SuppressWarnings("UnstableApiUsage")
public final class AxisUnitConverterSequence extends AbstractAxisUnitConverter implements AbstractAxisUnitConverter.Sequence {
    @NonNull
    private final ImmutableList<AxisUnitConverter> converters;

    @Override
    public boolean isIdentity() {
        return this.converters.stream().allMatch(AxisUnitConverter::isIdentity);
    }

    @Override
    public double convert(double value) {
        for (int i = 0; i < this.converters.size(); i++) {
            value = this.converters.get(i).convert(value);
        }
        return value;
    }

    @Override
    protected AxisUnitConverter inverse0() {
        return new AxisUnitConverterSequence(this.converters.reverse().stream().map(AxisUnitConverter::inverse).collect(ImmutableList.toImmutableList()));
    }

    private static <T> ImmutableList<T> maybeRemap(@NonNull ImmutableList<T> origList, @NonNull Function<? super T, ? extends T> remapper) {
        for (int i = 0; i < origList.size(); i++) {
            T origValue = origList.get(i);
            T remappedValue = remapper.apply(origValue);

            if (origValue != remappedValue) { //the remapping function returned a different value, so the results have changed and we need to build a new list with the results
                ImmutableList.Builder<T> nextListBuilder = ImmutableList.builder();

                nextListBuilder.addAll(origList.subList(0, i)); //append all the previous elements (which were unmodified)
                nextListBuilder.add(remappedValue);

                //remap and append all remaining elements
                for (i++; i < origList.size(); i++) {
                    nextListBuilder.add(remapper.apply(origList.get(i)));
                }

                return nextListBuilder.build();
            }
        }

        //no values were modified
        return origList;
    }

    private static <T> ImmutableList<T> maybeMerge2Neighbors(@NonNull ImmutableList<T> origList, @NonNull BiFunction<? super T, ? super T, ? extends T> merger) {
        for (int i = 1; i < origList.size(); i++) {
            T mergedValue = merger.apply(origList.get(i - 1), origList.get(i));

            if (mergedValue != null) {
                return ImmutableList.<T>builder()
                        .addAll(origList.subList(0, i - 1))
                        .add(mergedValue)
                        .addAll(origList.subList(i + 1, origList.size()))
                        .build();
            }
        }

        //no values were merged
        return origList;
    }

    private static <T> ImmutableList<T> maybeMerge3Neighbors(@NonNull ImmutableList<T> origList, @NonNull TriFunction<? super T, ? super T, ? super T, Iterable<T>> merger) {
        for (int i = 2; i < origList.size(); i++) {
            Iterable<T> mergedValues = merger.apply(origList.get(i - 2), origList.get(i - 1), origList.get(i));

            if (mergedValues != null) {
                return ImmutableList.<T>builder()
                        .addAll(origList.subList(0, i - 2))
                        .addAll(mergedValues)
                        .addAll(origList.subList(i + 1, origList.size()))
                        .build();
            }
        }

        //no values were merged
        return origList;
    }

    private static boolean isInverseFactors(double a, double b) {
        //check both ways to account for floating-point error
        return a == 1.0d / b || 1.0d / a == b;
    }

    @Override
    protected AxisUnitConverter simplify(boolean intern) {
        ImmutableList<AxisUnitConverter> converters = this.converters;
        ImmutableList<AxisUnitConverter> prevConverters;

        //this loop will keep running until no more optimizations can be made
        do {
            prevConverters = converters;

            //special handling for special converter counts
            switch (converters.size()) {
                case 0:
                    return maybeIntern(AxisUnitConverterIdentity.instance(), intern);
                case 1:
                    return maybeIntern(converters.get(0).simplify(), intern);
            }

            //simplify the converters
            converters = maybeRemap(converters, AxisUnitConverter::simplify);

            //remove identity converters
            if (converters.stream().anyMatch(AxisUnitConverter::isIdentity)) {
                converters = converters.stream()
                        .filter(((Predicate<? super AxisUnitConverter>) AxisUnitConverter::isIdentity).negate())
                        .collect(ImmutableList.toImmutableList());
                continue;
            }

            //flatten nested converter sequences
            if (converters.stream().anyMatch(AxisUnitConverterSequence.class::isInstance)) {
                converters = converters.stream()
                        .flatMap(converter -> converter instanceof AxisUnitConverterSequence
                                ? ((AxisUnitConverterSequence) converter).converters.stream()
                                : Stream.of(converter))
                        .collect(ImmutableList.toImmutableList());
            }

            //try to merge neighboring converters
            converters = maybeMerge2Neighbors(converters, (first, second) -> {
                if (first.getClass() == second.getClass()) { //both are the same type
                    if (first instanceof AxisUnitConverterAdd) {
                        return new AxisUnitConverterAdd(((AxisUnitConverterAdd) first).offset() + ((AxisUnitConverterAdd) second).offset());
                    } else if (first instanceof AxisUnitConverterMultiply) {
                        double firstFactor = ((AxisUnitConverterMultiply) first).factor();
                        double secondFactor = ((AxisUnitConverterMultiply) second).factor();
                        if (isInverseFactors(firstFactor, secondFactor)) { //one is the inverse of the other
                            return AxisUnitConverterIdentity.instance();
                        } else {
                            return new AxisUnitConverterMultiply(firstFactor * secondFactor);
                        }
                    }
                }

                return null;
            });

            converters = maybeMerge3Neighbors(converters, (a, b, c) -> {
                if (a instanceof AxisUnitConverterMultiply && b instanceof AxisUnitConverterAdd && c instanceof AxisUnitConverterMultiply) {
                    double aFactor = ((AxisUnitConverterMultiply) a).factor();
                    double bOffset = ((AxisUnitConverterAdd) b).offset();
                    double cFactor = ((AxisUnitConverterMultiply) c).factor();

                    if (isInverseFactors(aFactor, cFactor)) {
                        return ImmutableList.of(new AxisUnitConverterAdd(1.0d / aFactor == cFactor
                                ? bOffset / aFactor // (value * a + b) / a = value + (b / a)
                                : bOffset * cFactor // ((value / c) + b) * c = value + b * c
                        ));
                    } else { // (value * a + b) * c = value * a * c + b * c
                        return ImmutableList.of(new AxisUnitConverterMultiply(aFactor * cFactor), new AxisUnitConverterAdd(bOffset * cFactor));
                    }
                } else if (a instanceof AxisUnitConverterAdd && b instanceof AxisUnitConverterMultiply && c instanceof AxisUnitConverterAdd) {
                    double aOffset = ((AxisUnitConverterAdd) a).offset();
                    double bFactor = ((AxisUnitConverterMultiply) b).factor();
                    double cOffset = ((AxisUnitConverterAdd) c).offset();

                    // ((value + a) * b) + c = value * b + (a * b + c)
                    return ImmutableList.of(new AxisUnitConverterMultiply(bFactor), new AxisUnitConverterAdd(aOffset * bFactor + cOffset));
                }

                return null;
            });

            //TODO: maybe try to apply more optimizations?
        } while (converters != prevConverters);

        //intern the converters if requested
        if (intern) {
            converters = maybeRemap(converters, AxisUnitConverter::intern);
        }

        return converters == this.converters ? this : new AxisUnitConverterSequence(converters);
    }

    @Override
    protected AxisUnitConverter tryAndThen(@NonNull AxisUnitConverter next) {
        if (next instanceof AxisUnitConverterSequence) { //concatenate the two sequences, as otherwise we'd end up with a sequence of sequences (which would be stupid)
            return new AxisUnitConverterSequence(ImmutableList.<AxisUnitConverter>builder()
                    .addAll(this.converters)
                    .addAll(((AxisUnitConverterSequence) next).converters)
                    .build());
        }

        //TODO: do we care about trying to do further simplifications here?

        return super.tryAndThen(next);
    }
}
