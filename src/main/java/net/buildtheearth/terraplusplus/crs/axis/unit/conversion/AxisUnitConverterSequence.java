package net.buildtheearth.terraplusplus.crs.axis.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnitConverter;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author DaPorkchop_
 */
@Data
@EqualsAndHashCode(callSuper = false, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@SuppressWarnings("UnstableApiUsage")
public final class AxisUnitConverterSequence extends AbstractAxisUnitConverter {
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

            if (origList != remappedValue) { //the remapping function returned a different value, so the results have changed and we need to build a new list with the results
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
            }

            //flatten nested converter sequences
            if (converters.stream().anyMatch(AxisUnitConverterSequence.class::isInstance)) {
                converters = converters.stream()
                        .flatMap(converter -> converter instanceof AxisUnitConverterSequence
                                ? ((AxisUnitConverterSequence) converter).converters.stream()
                                : Stream.of(converter))
                        .collect(ImmutableList.toImmutableList());
            }

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
