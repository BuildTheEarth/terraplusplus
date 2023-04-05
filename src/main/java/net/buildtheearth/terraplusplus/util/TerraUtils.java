package net.buildtheearth.terraplusplus.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.util.PorkUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    /**
     * TODO produceZYZRotationMatrix javadoc
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static double[][] produceZYZRotationMatrix(double a, double b, double c) {

        double sina = Math.sin(a);
        double cosa = Math.cos(a);
        double sinb = Math.sin(b);
        double cosb = Math.cos(b);
        double sinc = Math.sin(c);
        double cosc = Math.cos(c);

        double[][] mat = new double[3][3];
        mat[0][0] = cosa * cosb * cosc - sinc * sina;
        mat[0][1] = -sina * cosb * cosc - sinc * cosa;
        mat[0][2] = cosc * sinb;

        mat[1][0] = sinc * cosb * cosa + cosc * sina;
        mat[1][1] = cosc * cosa - sinc * cosb * sina;
        mat[1][2] = sinc * sinb;

        mat[2][0] = -sinb * cosa;
        mat[2][1] = sinb * sina;
        mat[2][2] = cosb;

        return mat;
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
}
