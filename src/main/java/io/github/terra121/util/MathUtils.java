package io.github.terra121.util;

import lombok.experimental.UtilityClass;

import static net.daporkchop.lib.common.util.PValidation.*;

@UtilityClass
public class MathUtils {
    /**
     * Square root of 3
     */
    public static final double ROOT3 = Math.sqrt(3);

    /**
     * Two times pi
     */
    public static final double TAU = 2 * Math.PI;


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
}
