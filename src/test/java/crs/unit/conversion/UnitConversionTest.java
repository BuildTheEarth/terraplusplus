package crs.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.unit.UnitConverter;
import net.buildtheearth.terraplusplus.crs.unit.conversion.UnitConverterAdd;
import net.buildtheearth.terraplusplus.crs.unit.conversion.UnitConverterIdentity;
import net.buildtheearth.terraplusplus.crs.unit.conversion.UnitConverterMultiply;
import net.buildtheearth.terraplusplus.crs.unit.conversion.UnitConverterMultiplyAdd;
import net.buildtheearth.terraplusplus.crs.unit.conversion.UnitConverterSequence;
import org.junit.Test;

import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public class UnitConversionTest {
    @Test
    public void testSimplifiedPrecision() {
        Stream.of(
                new UnitConverterAdd(0.0d),
                new UnitConverterAdd(1.0d),
                new UnitConverterAdd(-1.5d),
                new UnitConverterAdd(Math.PI),

                new UnitConverterMultiply(0.0d),
                new UnitConverterMultiply(1.0d),
                new UnitConverterMultiply(-1.5d),
                new UnitConverterMultiply(Math.PI),

                new UnitConverterSequence(ImmutableList.of(
                        new UnitConverterAdd(1.0d),
                        new UnitConverterMultiply(Math.PI),
                        new UnitConverterAdd(Math.PI))),

                new UnitConverterSequence(ImmutableList.of(
                        new UnitConverterAdd(1.0d),
                        new UnitConverterAdd(1.0d),
                        new UnitConverterMultiply(Math.PI),
                        new UnitConverterAdd(Math.PI))),

                new UnitConverterSequence(ImmutableList.of(
                        new UnitConverterAdd(1.0d),
                        new UnitConverterAdd(-1.0d),
                        new UnitConverterMultiply(Math.PI),
                        new UnitConverterAdd(Math.PI))),

                new UnitConverterSequence(ImmutableList.of(
                        new UnitConverterAdd(1.0d),
                        new UnitConverterMultiply(Math.PI),
                        new UnitConverterMultiply(Math.PI),
                        new UnitConverterAdd(Math.PI))),

                new UnitConverterSequence(ImmutableList.of(
                        new UnitConverterAdd(1.0d),
                        new UnitConverterMultiply(Math.PI),
                        new UnitConverterMultiply(Math.PI).inverse(),
                        new UnitConverterAdd(Math.PI))),

                new UnitConverterSequence(ImmutableList.of(
                        new UnitConverterMultiply(1.5d),
                        new UnitConverterAdd(Math.PI),
                        new UnitConverterAdd(Math.PI).inverse(),
                        new UnitConverterMultiply(Math.PI))),

                new UnitConverterSequence(ImmutableList.of(
                        new UnitConverterMultiply(1.5d),
                        new UnitConverterAdd(Math.PI),
                        new UnitConverterAdd(Math.PI),
                        new UnitConverterAdd(Math.PI).inverse(),
                        new UnitConverterMultiply(Math.PI))),

                new UnitConverterSequence(ImmutableList.of(
                        new UnitConverterAdd(1.5d),
                        new UnitConverterMultiply(Math.PI),
                        new UnitConverterMultiply(Math.PI).inverse(),
                        new UnitConverterAdd(Math.PI))),

                new UnitConverterSequence(ImmutableList.of(
                        new UnitConverterAdd(1.5d),
                        new UnitConverterMultiply(Math.PI),
                        new UnitConverterMultiply(Math.PI),
                        new UnitConverterMultiply(Math.PI).inverse(),
                        new UnitConverterAdd(Math.PI)))
        ).parallel().forEach(originalConverter -> {
            UnitConverter simplifiedConverter = originalConverter.simplify();

            ThreadLocalRandom.current().doubles(1 << 20).parallel().forEach(value -> {
                double convertedOriginal = originalConverter.convert(value);
                double convertedCompiled = simplifiedConverter.convert(value);

                checkState(approxEquals(convertedOriginal, convertedCompiled, 1e-14));
            });
        });
    }

    private static boolean approxEquals(double a, double b, double d) {
        return Math.abs(a - b) <= d;
    }

    @Test
    public void testSimplification() {
        IntStream.range(0, 1 << 10).parallel().forEach(seed -> {
            SplittableRandom rng = new SplittableRandom(seed);

            UnitConverter originalConverter = randomConverterSequence(rng, rng.nextInt(256));
            UnitConverter simplifiedConverter = originalConverter.simplify();

            checkState(originalConverter.isIdentity() == simplifiedConverter.isIdentity(), "for seed %d: original=%s, simplified=%s", seed, originalConverter, simplifiedConverter);
            if (originalConverter.isIdentity()) {
                return;
            }

            checkState(simplifiedConverter instanceof UnitConverterAdd
                       || simplifiedConverter instanceof UnitConverterMultiply
                       || simplifiedConverter instanceof UnitConverterMultiplyAdd,
                    "for seed %d: original=%s, simplified=%s", seed, originalConverter, simplifiedConverter);
        });
    }

    private static UnitConverter randomConverterSequence(@NonNull SplittableRandom rng, int count) {
        switch (count) {
            case 0:
                return UnitConverterIdentity.instance();
            case 1:
                return randomConverter(rng);
            default:
                ImmutableList.Builder<UnitConverter> builder = ImmutableList.builder();
                for (int i = 0; i < count; ) {
                    if (rng.nextInt(64) != 0) {
                        builder.add(randomConverter(rng));
                        i++;
                    } else {
                        int batchCount = rng.nextInt(count - i);
                        builder.add(randomConverterSequence(rng, batchCount));
                        i += batchCount;
                    }
                }
                return new UnitConverterSequence(builder.build());
        }
    }

    private static UnitConverter randomConverter(@NonNull SplittableRandom rng) {
        double d = rng.nextDouble();
        return rng.nextBoolean() ? new UnitConverterAdd(d) : new UnitConverterMultiply(d);
    }
}
