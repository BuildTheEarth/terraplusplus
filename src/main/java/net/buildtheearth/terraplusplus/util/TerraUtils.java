package net.buildtheearth.terraplusplus.util;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.function.plain.TriFunction;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class TerraUtils {
    /**
     * Square root of 3
     */
    public static final double ROOT3 = Math.sqrt(3);
    /**
     * Two times pi
     */
    public static final double TAU = 2 * Math.PI;

    public static ITextComponent translate(String key) {
        if (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) {
            return new TextComponentTranslation(key);
        }
        return new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocal(key));
    }

    public static ITextComponent format(String key, Object... args) {
        if (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) {
            return new TextComponentTranslation(key, args);
        }
        return new TextComponentString(I18n.translateToLocalFormatted(key, args));
    }

    /**
     * Converts geographic latitude and longitude coordinates to spherical coordinates on a sphere of radius 1.
     *
     * @param geo - geographic coordinates as a double array of length 2, {longitude, latitude}, in degrees
     * @return the corresponding spherical coordinates in radians: {longitude, colatitude}
     */
    public static double[] geo2Spherical(double[] geo) {
        double lambda = Math.toRadians(geo[0]);
        double phi = Math.toRadians(90 - geo[1]);
        return new double[]{ lambda, phi };
    }

    public static void geo2Spherical(Vector2d geo, Vector2d dst) {
        geo2Spherical(geo.x, geo.y, dst);
    }

    public static void geo2Spherical(double longitude, double latitude, Vector2d dst) {
        dst.x = Math.toRadians(longitude);
        dst.y = Math.toRadians(90.0d - latitude);
    }

    /**
     * Converts spherical coordinates to geographic coordinates on a sphere of radius 1.
     *
     * @param spherical - spherical coordinates in radians as a double array of length 2: {longitude, colatitude}
     * @return the corresponding geographic coordinates in degrees: {longitude, latitude}
     */
    public static double[] spherical2Geo(double[] spherical) {
        double lon = Math.toDegrees(spherical[0]);
        double lat = 90 - Math.toDegrees(spherical[1]);
        return new double[]{ lon, lat };
    }

    public static void spherical2Geo(Vector2d spherical, Vector2d dst) {
        spherical2Geo(spherical.x, spherical.y, dst);
    }

    public static void spherical2Geo(double longitude, double colatitude, Vector2d dst) {
        dst.x = Math.toDegrees(longitude);
        dst.y = 90.0d - Math.toDegrees(colatitude);
    }

    /**
     * Converts spherical coordinates to Cartesian coordinates on a sphere of radius 1.
     *
     * @param spherical - spherical coordinates in radians as a double array of length 2: {longitude, colatitude}
     * @return the corresponding Cartesian coordinates: {x, y, z}
     */
    public static double[] spherical2Cartesian(double[] spherical) {
        double sinphi = Math.sin(spherical[1]);
        double x = sinphi * Math.cos(spherical[0]);
        double y = sinphi * Math.sin(spherical[0]);
        double z = Math.cos(spherical[1]);
        return new double[]{ x, y, z };
    }

    public static void spherical2Cartesian(Vector2d spherical, Vector3d dst) {
        spherical2Cartesian(spherical.x, spherical.y, dst);
    }

    /**
     * Converts spherical coordinates to Cartesian coordinates on a sphere of radius 1.
     *
     * @param longitude longitude in radians
     * @param colatitude colatitude in radians
     */
    public static void spherical2Cartesian(double longitude, double colatitude, Vector3d dst) {
        double sinphi = Math.sin(colatitude);
        dst.x = sinphi * Math.cos(longitude);
        dst.y = sinphi * Math.sin(longitude);
        dst.z = Math.cos(colatitude);
    }

    public static Matrix3x2 spherical2CartesianDerivative(double longitude, double colatitude) {
        Matrix3x2 result = Matrix3x2.createZero();
        spherical2CartesianDerivative(longitude, colatitude, result);
        return result;
    }

    public static void spherical2CartesianDerivative(double longitude, double colatitude, Matrix3x2 dst) {
        double sinlon = Math.sin(longitude);
        double coslon = Math.cos(longitude);
        double sinlat = Math.sin(colatitude);
        double coslat = Math.cos(colatitude);

        // https://www.wolframalpha.com/input?i=d%2Fdl+sin%28c%29+*+cos%28l%29
        dst.m00 = -sinlat * sinlon;

        // https://www.wolframalpha.com/input?i=d%2Fdc+sin%28c%29+*+cos%28l%29
        dst.m01 = coslat * coslon;

        // https://www.wolframalpha.com/input?i=d%2Fdl+sin%28c%29+*+sin%28l%29
        dst.m10 = sinlat * coslon;

        // https://www.wolframalpha.com/input?i=d%2Fdc+sin%28c%29+*+sin%28l%29
        dst.m11 = coslat * sinlon;

        // https://www.wolframalpha.com/input?i=d%2Fdl+cos%28c%29
        dst.m20 = 0.0d;

        // https://www.wolframalpha.com/input?i=d%2Fdc+cos%28c%29
        dst.m21 = -sinlat;
    }

    /**
     * Converts Cartesian coordinates to spherical coordinates on a sphere of radius 1.
     *
     * @param cartesian - Cartesian coordinates as double array of length 3: {x, y, z}
     * @return the spherical coordinates of the corresponding normalized vector
     */
    public static double[] cartesian2Spherical(double[] cartesian) {
        double lambda = Math.atan2(cartesian[1], cartesian[0]);
        double phi = Math.atan2(Math.sqrt(cartesian[0] * cartesian[0] + cartesian[1] * cartesian[1]), cartesian[2]);
        return new double[]{ lambda, phi };
    }

    public static Vector2d cartesian2Spherical(Vector3d cartesian) {
        Vector2d result = new Vector2d();
        cartesian2Spherical(cartesian.x, cartesian.y, cartesian.z, result);
        return result;
    }

    public static void cartesian2Spherical(double x, double y, double z, Vector2d dst) {
        double lambda = Math.atan2(y, x);
        double phi = Math.atan2(Math.sqrt(x * x + y * y), z);
        dst.x = lambda;
        dst.y = phi;
    }

    /**
     * TODO produceZYZRotationMatrix javadoc
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static Matrix3 produceZYZRotationMatrix(double a, double b, double c) {

        double sina = Math.sin(a);
        double cosa = Math.cos(a);
        double sinb = Math.sin(b);
        double cosb = Math.cos(b);
        double sinc = Math.sin(c);
        double cosc = Math.cos(c);

        Matrix3 mat = new Matrix3();
        mat.m00 = cosa * cosb * cosc - sinc * sina;
        mat.m01 = -sina * cosb * cosc - sinc * cosa;
        mat.m02 = cosc * sinb;

        mat.m10 = sinc * cosb * cosa + cosc * sina;
        mat.m11 = cosc * cosa - sinc * cosb * sina;
        mat.m12 = sinc * sinb;

        mat.m20 = -sinb * cosa;
        mat.m21 = sinb * sina;
        mat.m22 = cosb;

        return mat;
    }

    public static MatrixSIS matrixToSIS(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        MatrixSIS result = TMatrices.createZero(rows, cols);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                result.setElement(row, col, matrix[row][col]);
            }
        }
        return result;
    }

    /**
     * Multiples the given matrix with the given vector.
     * The matrix is assumed to be square and the vector is assumed to be of the same dimension as the matrix.
     *
     * @param matrix - the matrix as a n*n double array
     * @param vector - the vector as double array of length n
     * @return the result of the multiplication as an array of double on length n
     */
    public static double[] matVecProdD(double[][] matrix, double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    /**
     * Converts all values in a double array from degrees to radians
     *
     * @param arr - array to work on
     */
    public static void toRadians(double[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Math.toRadians(arr[i]);
        }
    }

    /**
     * Converts all values in a double array from radians to degrees
     *
     * @param arr - array to work on
     */
    public static void toDegrees(double[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Math.toDegrees(arr[i]);
        }
    }

    /**
     * Squares the given value.
     *
     * @param d the value to square
     * @return d²
     */
    public static double sq(double d) {
        return d * d;
    }

    /**
     * Right-shifts the given value by the given number of bits, safely handling negative shifts and checking for overflow.
     *
     * @param val   the value
     * @param shift the number of bits to shift by
     * @return the shifted value
     */
    public static int safeDirectionalShift(int val, int shift) {
        int res;
        if (shift == 0) {
            res = val;
        } else if (shift > 0) {
            res = val << shift;
            checkState(res >> shift == val, "numeric overflow: val: %d, shift: %d", val, shift);
        } else {
            res = val >> -shift;
        }
        return res;
    }

    public static ITextComponent title() {
        return new TextComponentString(TerraConstants.CHAT_PREFIX.replace("&", "\u00A7"));
    }

    public static ITextComponent titleAndCombine(Object... objects) {
        return combine(true, objects);
    }

    public static ITextComponent combine(Object... objects) {
        return combine(false, objects);
    }

    public static ITextComponent combine(boolean title, Object... objects) {
        ITextComponent textComponent = title ? title() : new TextComponentString("");
        StringBuilder builder = null;
        TextFormatting lastFormat = null;
        for (Object o : objects) {
            if (o instanceof ITextComponent) {
                if (builder != null) {
                    textComponent.appendSibling(new TextComponentString(builder.toString()));
                    builder = null;
                }

                ITextComponent component = (ITextComponent) o;
                if (component.getStyle().getColor() == null && lastFormat != null) {
                    component.setStyle(new Style().setColor(lastFormat));
                }

                textComponent.appendSibling(component);
            } else {
                if (o instanceof TextFormatting) {
                    lastFormat = (TextFormatting) o;
                }
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(o);
            }
        }

        if (builder != null) {
            textComponent.appendSibling(new TextComponentString(builder.toString()));
        }
        return textComponent;
    }

    public static ITextComponent getNotCC() {
        return titleAndCombine(TextFormatting.RED, translate(TerraConstants.MODID + ".error.notcc"));
    }

    public static ITextComponent getNotTerra() {
        return titleAndCombine(TextFormatting.RED, translate(TerraConstants.MODID + ".error.noterra"));
    }

    public static ITextComponent getNoPermission() {
        return titleAndCombine(TextFormatting.RED, "You do not have permission to use this command");
    }

    public static ITextComponent getPlayerOnly() {
        return titleAndCombine(TextFormatting.RED, translate(TerraConstants.MODID + ".error.playeronly"));
    }

    /**
     * Merges multiple {@link CompletableFuture}s together asynchronously.
     *
     * @param futures a {@link Stream} containing the {@link CompletableFuture}s to merge
     * @param <T>     the type of value
     * @return a {@link CompletableFuture} which will be notified once all of the futures have been completed
     */
    public <T> CompletableFuture<List<T>> mergeFuturesAsync(@NonNull Stream<CompletableFuture<T>> futures) {
        CompletableFuture<T>[] arr = uncheckedCast(futures.toArray(CompletableFuture[]::new));
        return CompletableFuture.allOf(arr).thenApply(unused -> Arrays.stream(arr).map(CompletableFuture::join).collect(Collectors.toList()));
    }

    /**
     * Compares two {@code double[]}s.
     *
     * @param v0 the first {@code double[]}
     * @param v1 the second {@code double[]}
     * @see Comparator#compare(Object, Object)
     */
    public int compareDoubleArrays(@NonNull double[] v0, @NonNull double[] v1) {
        int len0 = v0.length;
        int len1 = v1.length;

        for (int i = 0, lim = min(len0, len1); i < lim; i++) {
            int d = Double.compare(v0[i], v1[i]);
            if (d != 0) {
                return d;
            }
        }

        return len0 - len1;
    }

    /**
     * Converts the given {@code double} to {@code long} without loss of precision.
     *
     * @param value the {@code double}
     * @return the given value as a {@code long}
     * @throws ArithmeticException if the given value cannot be converted to {@code long} without loss of precision
     */
    public static long toLongExact(double value) throws ArithmeticException {
        if ((long) value != value) {
            throw new ArithmeticException("loss of precision when converting to long");
        }
        return (long) value;
    }

    /**
     * Converts the given {@link Number} to {@code long} without loss of precision.
     *
     * @param value the {@link Number}
     * @return the given value as a {@code long}
     * @throws ArithmeticException if the given value cannot be converted to {@code long} without loss of precision
     */
    public static long toLongExact(@NonNull Number value) throws ArithmeticException {
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            //these types can be converted to long without loss of precision
            return value.longValue();
        } else if (value instanceof Double || value instanceof Float) {
            //these types can be converted to double without loss of precision, and from there we can try to convert to long
            return toLongExact(value.doubleValue());
        } else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValueExact();
        } else if (value instanceof BigInteger) {
            return ((BigInteger) value).longValueExact();
        } else {
            throw new IllegalArgumentException("unknown Number type: " + PorkUtil.className(value));
        }
    }

    /**
     * Converts the given {@code long} to {@code double} without loss of precision.
     *
     * @param value the {@code long}
     * @return the given value as a {@code double}
     * @throws ArithmeticException if the given value cannot be converted to {@code double} without loss of precision
     */
    public static double toDoubleExact(long value) throws ArithmeticException {
        if ((long) (double) value != value) {
            throw new ArithmeticException("loss of precision when converting to double");
        }
        return value;
    }

    /**
     * Converts the given {@link BigInteger} to {@code double} without loss of precision.
     *
     * @param value the {@link BigInteger}
     * @return the given value as a {@code double}
     * @throws ArithmeticException if the given value cannot be converted to {@code double} without loss of precision
     */
    public static double toDoubleExact(@NonNull BigInteger value) throws ArithmeticException {
        return toDoubleExact(value.longValueExact());
    }

    /**
     * Converts the given {@link BigDecimal} to {@code double} without loss of precision.
     *
     * @param value the {@link BigDecimal}
     * @return the given value as a {@code double}
     * @throws ArithmeticException if the given value cannot be converted to {@code double} without loss of precision
     */
    public static double toDoubleExact(@NonNull BigDecimal value) throws ArithmeticException {
        //we could probably do some cool stuff like checking if the precision and scale are within the possible range, however this
        // lazier approach is guaranteed to work correctly
        double doubleValue = value.doubleValue();
        if (!BigDecimal.valueOf(doubleValue).equals(value)) {
            throw new ArithmeticException("loss of precision when converting to double");
        }
        return doubleValue;
    }

    /**
     * Converts the given {@link Number} to {@code double} without loss of precision.
     *
     * @param value the {@link Number}
     * @return the given value as a {@code double}
     * @throws ArithmeticException if the given value cannot be converted to {@code double} without loss of precision
     */
    public static double toDoubleExact(@NonNull Number value) throws ArithmeticException {
        if (value instanceof Double || value instanceof Integer || value instanceof Float
            || value instanceof Short || value instanceof Byte) {
            //these types can be converted to double without loss of precision
            return value.doubleValue();
        } else if (value instanceof Long) {
            return toDoubleExact(value.longValue());
        } else if (value instanceof BigDecimal) {
            return toDoubleExact((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            return toDoubleExact((BigInteger) value);
        } else {
            throw new IllegalArgumentException("unknown Number type: " + PorkUtil.className(value));
        }
    }

    /**
     * Converts the given {@link Number} to {@link BigDecimal} without loss of precision.
     *
     * @param value the {@link Number}
     * @return the given value as a {@link BigDecimal}
     * @throws ArithmeticException if the given value cannot be converted to {@link BigDecimal} without loss of precision
     */
    public static BigDecimal toBigDecimalExact(@NonNull Number value) throws ArithmeticException {
        if (value instanceof Double || value instanceof Float) {
            //these types can be converted to double without loss of precision, and from there we can convert losslessly to BigDecimal
            return BigDecimal.valueOf(value.doubleValue());
        } else if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            //these types can be converted to long without loss of precision, and from there we can convert losslessly to BigDecimal
            return BigDecimal.valueOf(value.longValue());
        } else if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        } else {
            throw new IllegalArgumentException("unknown Number type: " + PorkUtil.className(value));
        }
    }

    /**
     * Converts the given {@link Number} to {@link BigInteger} without loss of precision.
     *
     * @param value the {@link Number}
     * @return the given value as a {@link BigInteger}
     * @throws ArithmeticException if the given value cannot be converted to {@link BigInteger} without loss of precision
     */
    public static BigInteger toBigIntegerExact(@NonNull Number value) throws ArithmeticException {
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            //these types can be converted to long without loss of precision, and from there we can convert losslessly to BigInteger
            return BigInteger.valueOf(value.longValue());
        } else if (value instanceof Double || value instanceof Float) {
            //these types can be converted to double without loss of precision, from there we can try to convert to long, and
            // if that succeeds we can convert losslessly to BigInteger
            return BigInteger.valueOf(toLongExact(value.doubleValue()));
        } else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toBigIntegerExact();
        } else if (value instanceof BigInteger) {
            return (BigInteger) value;
        } else {
            throw new IllegalArgumentException("unknown Number type: " + PorkUtil.className(value));
        }
    }

    public static boolean isPrimitiveIntegerType(@NonNull Number value) {
        return value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte;
    }

    public static boolean isAnyIntegerType(@NonNull Number value) {
        return isPrimitiveIntegerType(value) || value instanceof BigInteger;
    }

    public static boolean isPrimitiveFloatingPointType(@NonNull Number value) {
        return value instanceof Double || value instanceof Float;
    }

    public static boolean isAnyFloatingPointType(@NonNull Number value) {
        return isPrimitiveFloatingPointType(value) || value instanceof BigDecimal;
    }

    public static boolean numbersEqual(@NonNull Number a, @NonNull Number b) {
        checkArg(isAnyIntegerType(a) || isAnyFloatingPointType(a), "unknown Number type: %s", a.getClass());
        checkArg(isAnyIntegerType(b) || isAnyFloatingPointType(b), "unknown Number type: %s", b.getClass());

        if (a.getClass() == b.getClass()) {
            //they are both the same numeric type, compare using Object#equals(Object)
            return a.equals(b);
        } else if (isPrimitiveIntegerType(a) && isPrimitiveIntegerType(b)) {
            //all primitive integral types can be converted to long without loss of precision
            return a.longValue() == b.longValue();
        } else if (isPrimitiveFloatingPointType(a) && isPrimitiveFloatingPointType(b)) {
            //all primitive floating-point types can be converted to double without loss of precision
            return a.doubleValue() == b.doubleValue();
        } else if (isAnyIntegerType(a) && isAnyIntegerType(b)) {
            //at least one of the two values is a BigInteger, so we'll convert both values to BigInteger and compare those (could be optimized more, i don't care)
            return toBigIntegerExact(a).equals(toBigIntegerExact(b));
        } else {
            //at least one of the two values is a BigDecimal, so we'll convert both values to BigDecimal and compare those (could be optimized more, i don't care)
            return toBigDecimalExact(a).compareTo(toBigDecimalExact(b)) == 0;
        }
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
    public static <T> ImmutableList<T> maybeFlatten(@NonNull ImmutableList<T> origList, @NonNull Function<? super T, ? extends Iterable<? extends T>> flattener) {
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
    public static <T> ImmutableList<T> maybeRemap(@NonNull ImmutableList<T> origList, @NonNull Function<? super T, ? extends T> remapper) {
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
    public static <T> ImmutableList<T> maybeRemove(@NonNull ImmutableList<T> origList, @NonNull Predicate<? super T> shouldRemove) {
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
    public static <T> ImmutableList<T> maybeMerge2Neighbors(@NonNull ImmutableList<T> origList, @NonNull BiFunction<? super T, ? super T, ? extends T> merger) {
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
    public static <T> ImmutableList<T> maybeMerge3Neighbors(@NonNull ImmutableList<T> origList, @NonNull TriFunction<? super T, ? super T, ? super T, ImmutableList<? extends T>> merger) {
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

    /**
     * {@link Internable#intern() Interns} the elements of the given {@link ImmutableList}.
     *
     * @return an {@link ImmutableList} containing the interned representations of the elements of the original list
     */
    public static <T extends Internable<? super T>> ImmutableList<T> internElements(@NonNull ImmutableList<T> origList) {
        return maybeRemap(origList, uncheckedCast((Function<Internable<?>, Object>) Internable::intern));
    }

    /**
     * Concatenates the given {@link ImmutableList}s.
     */
    public static <T> ImmutableList<T> concat(@NonNull ImmutableList<? extends T> l, @NonNull ImmutableList<? extends T> r) {
        return ImmutableList.<T>builder().addAll(l).addAll(r).build();
    }
}
