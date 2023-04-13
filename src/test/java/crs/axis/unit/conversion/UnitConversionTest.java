package crs.axis.unit.conversion;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnitConverter;
import net.buildtheearth.terraplusplus.crs.axis.unit.conversion.AxisUnitConverterAdd;
import net.buildtheearth.terraplusplus.crs.axis.unit.conversion.AxisUnitConverterIdentity;
import net.buildtheearth.terraplusplus.crs.axis.unit.conversion.AxisUnitConverterMultiply;
import net.buildtheearth.terraplusplus.crs.axis.unit.conversion.AxisUnitConverterSequence;
import net.buildtheearth.terraplusplus.crs.axis.unit.conversion.CompiledAxisUnitConverter;
import org.junit.Test;

import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public class UnitConversionTest {
    @Test
    public void testCompile() {
        testCompile(new AxisUnitConverterAdd(0.0d));
        testCompile(new AxisUnitConverterAdd(1.0d));
        testCompile(new AxisUnitConverterAdd(-1.5d));
        testCompile(new AxisUnitConverterAdd(Math.PI));

        testCompile(new AxisUnitConverterMultiply(0.0d));
        testCompile(new AxisUnitConverterMultiply(1.0d));
        testCompile(new AxisUnitConverterMultiply(-1.5d));
        testCompile(new AxisUnitConverterMultiply(Math.PI));

        testCompile(new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterAdd(1.0d),
                new AxisUnitConverterMultiply(Math.PI),
                new AxisUnitConverterAdd(Math.PI))));

        testCompile(new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterAdd(1.0d),
                new AxisUnitConverterAdd(1.0d),
                new AxisUnitConverterMultiply(Math.PI),
                new AxisUnitConverterAdd(Math.PI))));

        testCompile(new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterAdd(1.0d),
                new AxisUnitConverterAdd(-1.0d),
                new AxisUnitConverterMultiply(Math.PI),
                new AxisUnitConverterAdd(Math.PI))));

        testCompile(new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterAdd(1.0d),
                new AxisUnitConverterMultiply(Math.PI),
                new AxisUnitConverterMultiply(Math.PI),
                new AxisUnitConverterAdd(Math.PI))));

        testCompile(new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterAdd(1.0d),
                new AxisUnitConverterMultiply(Math.PI),
                new AxisUnitConverterMultiply(Math.PI).inverse(),
                new AxisUnitConverterAdd(Math.PI))));

        testCompile(new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterMultiply(1.5d),
                new AxisUnitConverterAdd(Math.PI),
                new AxisUnitConverterAdd(Math.PI).inverse(),
                new AxisUnitConverterMultiply(Math.PI))));

        testCompile(new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterMultiply(1.5d),
                new AxisUnitConverterAdd(Math.PI),
                new AxisUnitConverterAdd(Math.PI),
                new AxisUnitConverterAdd(Math.PI).inverse(),
                new AxisUnitConverterMultiply(Math.PI))));

        testCompile(new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterAdd(1.5d),
                new AxisUnitConverterMultiply(Math.PI),
                new AxisUnitConverterMultiply(Math.PI).inverse(),
                new AxisUnitConverterAdd(Math.PI))));

        testCompile(new AxisUnitConverterSequence(ImmutableList.of(
                new AxisUnitConverterAdd(1.5d),
                new AxisUnitConverterMultiply(Math.PI),
                new AxisUnitConverterMultiply(Math.PI),
                new AxisUnitConverterMultiply(Math.PI).inverse(),
                new AxisUnitConverterAdd(Math.PI))));
    }

    private static void testCompile(@NonNull AxisUnitConverter originalConverter) {
        //System.out.printf("testing %s\nsimplified: %s\n\n", originalConverter, originalConverter.simplify());

        AxisUnitConverter compiledConverter = CompiledAxisUnitConverter.compile(originalConverter);

        ThreadLocalRandom.current().doubles(1 << 20).forEach(value -> {
            double convertedOriginal = originalConverter.convert(value);
            double convertedCompiled = compiledConverter.convert(value);

            checkState(approxEquals(convertedOriginal, convertedCompiled, 1e-14));
        });
    }

    private static boolean approxEquals(double a, double b, double d) {
        return Math.abs(a - b) <= d;
    }

    @Test
    public void testSimplification() {
        IntStream.range(0, 1 << 25).parallel().forEach(seed -> {
            SplittableRandom rng = new SplittableRandom(seed);

            AxisUnitConverter originalConverter = randomConverterSequence(rng, rng.nextInt(32));
            AxisUnitConverter simplifiedConverter = originalConverter.simplify();

            checkState(originalConverter.isIdentity() == simplifiedConverter.isIdentity(), "for seed %d: original=%s, simplified=%s", seed, originalConverter, simplifiedConverter);
            if (originalConverter.isIdentity()) {
                return;
            }

            checkState(simplifiedConverter instanceof AxisUnitConverterAdd
                       || simplifiedConverter instanceof AxisUnitConverterMultiply
                       || (simplifiedConverter instanceof AxisUnitConverterSequence && ((AxisUnitConverterSequence) simplifiedConverter).converters().size() == 2),
                    "for seed %d: original=%s, simplified=%s", seed, originalConverter, simplifiedConverter);
        });
    }

    private static AxisUnitConverter randomConverterSequence(@NonNull SplittableRandom rng, int count) {
        switch (count) {
            case 0:
                return AxisUnitConverterIdentity.instance();
            case 1:
                return randomConverter(rng);
            default:
                ImmutableList.Builder<AxisUnitConverter> builder = ImmutableList.builder();
                for (int i = 0; i < count; i++) {
                    builder.add(randomConverter(rng));
                }
                return new AxisUnitConverterSequence(builder.build());
        }
    }

    private static AxisUnitConverter randomConverter(@NonNull SplittableRandom rng) {
        double d = rng.nextDouble();
        return rng.nextBoolean() ? new AxisUnitConverterAdd(d) : new AxisUnitConverterMultiply(d);
    }
}
