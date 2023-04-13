package net.buildtheearth.terraplusplus.crs.axis.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnitConverter;
import net.daporkchop.lib.common.function.plain.TriFunction;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author DaPorkchop_
 */
@Data
@EqualsAndHashCode(callSuper = false, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@SuppressWarnings("UnstableApiUsage")
public final class AxisUnitConverterSequence extends AbstractAxisUnitConverter implements AbstractAxisUnitConverter.RepresentableAsSequence {
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

    @Override
    public AxisUnitConverterSequence asConverterSequence() {
        return this;
    }

    private static <T> void addRangeTo(@NonNull ImmutableList<? extends T> src, int srcBegin, int srcEnd, @NonNull ImmutableList.Builder<? super T> dst) {
        if (srcBegin == srcEnd) {
            return;
        } else if (srcBegin == 0 && srcEnd == src.size()) {
            dst.addAll(src);
            return;
        }

        for (int i = srcBegin; i < srcEnd; i++) {
            dst.add(src.get(i));
        }
    }

    private static <T> void singleFlattenInto(@NonNull T value, @NonNull ImmutableList.Builder<? super T> dst, @NonNull Function<? super T, ? extends Iterable<? extends T>> flattener) {
        Iterable<? extends T> flattenedValues = flattener.apply(value);
        if (flattenedValues != null) {
            flattenInto(flattenedValues, dst, flattener);
        } else {
            dst.add(value);
        }
    }

    private static <T> void flattenInto(@NonNull Iterable<? extends T> src, @NonNull ImmutableList.Builder<? super T> dst, @NonNull Function<? super T, ? extends Iterable<? extends T>> flattener) {
        for (T value : src) {
            Iterable<? extends T> flattenedValues = flattener.apply(value);
            if (flattenedValues != null) {
                flattenInto(flattenedValues, dst, flattener);
            } else {
                dst.add(value);
            }
        }
    }

    /**
     * @param flattener a function which either returns an {@link Iterable} containing the new value(s) to insert in place of the existing
     *                  value, or {@code null} to leave the existing value unmodified
     */
    private static <T> ImmutableList<T> maybeFlatten(@NonNull ImmutableList<T> origList, @NonNull Function<? super T, ? extends Iterable<? extends T>> flattener) {
        for (int i = 0; i < origList.size(); i++) {
            Iterable<? extends T> flattenedValues = flattener.apply(origList.get(i));

            if (flattenedValues != null) {
                ImmutableList.Builder<T> builder = ImmutableList.builder();

                addRangeTo(origList, 0, i, builder);
                flattenInto(flattenedValues, builder, flattener);

                while (++i < origList.size()) {
                    singleFlattenInto(origList.get(i), builder, flattener);
                }

                return builder.build();
            }
        }

        return origList;
    }

    /**
     * @param remapper a function which either returns the value to replace the existing value with
     */
    private static <T> ImmutableList<T> maybeRemap(@NonNull ImmutableList<T> origList, @NonNull Function<? super T, ? extends T> remapper) {
        for (int i = 0; i < origList.size(); i++) {
            T origValue = origList.get(i);
            T remappedValue = remapper.apply(origValue);

            if (origValue != remappedValue) { //the remapping function returned a different value, so the results have changed and we need to build a new list with the results
                ImmutableList.Builder<T> builder = ImmutableList.builder();

                addRangeTo(origList, 0, i, builder); //append all the previous elements (which were unmodified)
                builder.add(remappedValue);

                //remap and append all remaining elements
                while (++i < origList.size()) {
                    builder.add(remapper.apply(origList.get(i)));
                }

                return builder.build();
            }
        }

        //no values were modified
        return origList;
    }

    /**
     * @param shouldRemove a predicate which returns {@code true} if the value should be removed
     */
    private static <T> ImmutableList<T> maybeRemove(@NonNull ImmutableList<T> origList, @NonNull Predicate<? super T> shouldRemove) {
        for (int i = 0; i < origList.size(); i++) {
            if (shouldRemove.test(origList.get(i))) {
                ImmutableList.Builder<T> builder = ImmutableList.builder();

                addRangeTo(origList, 0, i, builder); //append all the previous elements (which were all kept)
                while (++i < origList.size()) {
                    T origValue = origList.get(i);
                    if (!shouldRemove.test(origValue)) {
                        builder.add(origValue);
                    }
                }

                return builder.build();
            }
        }

        //no values were removed
        return origList;
    }

    /**
     * @param merger a function which returns a single value as the result of merging the two given values, or {@code null} if both values should be kept
     */
    private static <T> ImmutableList<T> maybeMerge2Neighbors(@NonNull ImmutableList<T> origList, @NonNull BiFunction<? super T, ? super T, ? extends T> merger) {
        if (origList.size() < 2) { //list is too small to do anything with
            return origList;
        }

        ImmutableList.Builder<T> builder = null;

        // a: the neighboring element at the lower index, may be either the value of origList.get(bIndex - 1) or a merge output if the previous merger invocation
        //    actually merged something
        // b: the neighboring element at the higher index, always the value of origList.get(bIndex)

        T a = origList.get(0);
        T b;
        int bIndex = 1;

        do {
            b = origList.get(bIndex);

            T mergedValue = merger.apply(a, b);

            if (mergedValue != null) { //the two values were merged into a single result!
                if (builder == null) { //this is the first time anything has been merged so far
                    //create a new builder (setting this will ensure that all subsequently encountered values will be written out to the builder,
                    // as we know that we won't be returning the original input list)
                    builder = ImmutableList.builder();

                    //append all the previous elements in the range [0, a) - none of them were able to be merged, so we can copy them in now that
                    // we know SOMETHING will change)
                    addRangeTo(origList, 0, bIndex - 1, builder);
                }

                //set a to the merge result, so that we can immediately try to merge it again with the next value in the list (the value of b will
                // be ignored, which is correct since it's now part of mergedValue, which is now a)
                a = mergedValue;
            } else { //we weren't able to merge the two values
                if (builder != null) { //a previous merge has succeeded, so we should add the old value to the new list even though nothing changed
                    builder.add(a);
                }

                //prepare to advance by one element
                a = b;
            }
        } while (++bIndex < origList.size());

        if (builder != null) { //at least one merge succeeded, add the last value of a and return the newly constructed list
            return builder.add(a).build();
        } else { //nothing changed
            return origList;
        }
    }

    /**
     * @param merger a function which returns an {@link ImmutableList} containing the value(s) resulting from merging the three given values, or
     *               {@code null} if all three values should be kept
     */
    private static <T> ImmutableList<T> maybeMerge3Neighbors(@NonNull ImmutableList<T> origList, @NonNull TriFunction<? super T, ? super T, ? super T, ImmutableList<? extends T>> merger) {
        if (origList.size() < 3) { //list is too small to do anything with
            return origList;
        }

        ImmutableList.Builder<T> builder = null;

        // a: the neighboring element at the lowest index, may be either the value of origList.get(cIndex - 2) or the first merge output if the previous merger invocation
        //    succeeded and produced one or two elements
        // b: the neighboring element at the middle index, may be either the value of origList.get(cIndex - 1) or the second merge output if the previous merger invocation
        //    succeeded and produced exactly two elements
        // c: the neighboring element at the highest index, always the value of origList.get(cIndex)

        T a = Objects.requireNonNull(origList.get(0));
        T b = Objects.requireNonNull(origList.get(1));
        T c;
        int cIndex = 2;

        do {
            c = Objects.requireNonNull(origList.get(cIndex));

            ImmutableList<? extends T> mergedValues = merger.apply(a, b, c);

            if (mergedValues != null) { //the two values were merged into a single result!
                if (builder == null) { //this is the first time anything has been merged so far
                    //create a new builder (setting this will ensure that all subsequently encountered values will be written out to the builder,
                    // as we know that we won't be returning the original input list)
                    builder = ImmutableList.builder();

                    //append all the previous elements in the range [0, a) - none of them were able to be merged, so we can copy them in now that
                    // we know SOMETHING will change)
                    addRangeTo(origList, 0, cIndex - 2, builder);
                }

                switch (mergedValues.size()) {
                    case 0: //merging produced no outputs, skip everything and try to advance twice
                        a = ++cIndex < origList.size() ? Objects.requireNonNull(origList.get(cIndex)) : null;
                        b = ++cIndex < origList.size() ? Objects.requireNonNull(origList.get(cIndex)) : null;
                        //c will be loaded automatically during the next loop iteration, or not if there isn't enough space
                        break;
                    case 1:
                        //set a to the merge result, so that we can immediately try to merge it again with the next two values in the list (the values of b and c will
                        // be ignored, which is correct since it's now part of mergedValues, which is now a)
                        a = Objects.requireNonNull(mergedValues.get(0));
                        b = ++cIndex < origList.size() ? Objects.requireNonNull(origList.get(cIndex)) : null;
                        //c will be loaded automatically during the next loop iteration, or not if there isn't enough space
                        break;
                    case 2:
                        a = Objects.requireNonNull(mergedValues.get(0));
                        b = Objects.requireNonNull(mergedValues.get(1));
                        //c will be loaded automatically during the next loop iteration, or not if there isn't enough space
                        break;
                    default:
                        throw new UnsupportedOperationException("merger returned " + mergedValues.size() + " elements!");
                }
            } else { //we weren't able to merge the three values
                if (builder != null) { //a previous merge has succeeded, so we should add the old value to the new list even though nothing changed
                    builder.add(a);
                }

                //prepare to advance by one element
                a = b;
                b = c;
            }
        } while (++cIndex < origList.size());

        if (builder != null) { //at least one merge succeeded, add the last value of a (and b, if necessary) and return the newly constructed list

            //a or b can only be null if the last loop iteration resulted in a merge producing one output, and there weren't enough input values left
            // to load into (a and) b before terminating the loop
            if (a != null) {
                builder.add(a);
                if (b != null) {
                    builder.add(b);
                }
            }

            return builder.build();
        } else { //nothing changed
            return origList;
        }
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
        boolean flattened = false;
        do {
            prevConverters = converters;

            //special handling for special converter counts
            switch (converters.size()) {
                case 0:
                    return maybeIntern(AxisUnitConverterIdentity.instance(), intern);
                case 1:
                    return maybeIntern(converters.get(0).simplify(), intern);
            }

            //flatten nested converter sequences
            // (we only want to do this once, as none of the other transformations in this loop can actually cause any sequence converters
            // to be inserted into the sequence)
            if (!flattened) {
                flattened = true;
                converters = maybeFlatten(converters, converter -> converter instanceof RepresentableAsSequence
                        ? ((RepresentableAsSequence) converter).asConverterSequence().converters()
                        : null);
            }

            //simplify the converters
            converters = maybeRemap(converters, AxisUnitConverter::simplify);

            //remove identity converters
            converters = maybeRemove(converters, AxisUnitConverter::isIdentity);

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
        } while (converters != prevConverters);

        switch (converters.size()) {
            case 0:
            case 1:
                throw new IllegalStateException();
            case 2:
                //special cases with where there are two converters
                AxisUnitConverter a = converters.get(0);
                AxisUnitConverter b = converters.get(1);
                if (a instanceof AxisUnitConverterMultiply && b instanceof AxisUnitConverterAdd) { // [multiply, add] -> multiply and add
                    double aFactor = ((AxisUnitConverterMultiply) a).factor();
                    double bOffset = ((AxisUnitConverterAdd) b).offset();

                    return maybeIntern(new AxisUnitConverterMultiplyAdd(aFactor, bOffset), intern);
                } else if (a instanceof AxisUnitConverterAdd && b instanceof AxisUnitConverterMultiply) { // [add, multiply] -> multiply and add
                    double aOffset = ((AxisUnitConverterAdd) a).offset();
                    double bFactor = ((AxisUnitConverterMultiply) b).factor();

                    // (value + a) * b = value * b + a * b
                    return maybeIntern(new AxisUnitConverterMultiplyAdd(bFactor, aOffset * bFactor), intern);
                }
                break;
        }

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
