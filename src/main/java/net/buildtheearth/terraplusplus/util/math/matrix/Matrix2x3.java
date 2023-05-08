package net.buildtheearth.terraplusplus.util.math.matrix;

import org.apache.sis.internal.util.Numerics;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.Matrix3;
import org.apache.sis.referencing.operation.matrix.MismatchedMatrixSizeException;
import org.opengis.referencing.operation.Matrix;

/**
 * A {@link Matrix} with {@code 2} rows and {@code 3} columns.
 *
 * <blockquote><pre> ┌         ┐
 * │ {@linkplain #m00} {@linkplain #m01} {@linkplain #m02} │
 * │ {@linkplain #m10} {@linkplain #m11} {@linkplain #m12} │
 * └         ┘</pre></blockquote>
 *
 * @author DaPorkchop_
 * @see Matrix2
 * @see Matrix3
 * @see Matrix3x2
 */
public final class Matrix2x3 extends AbstractMatrixSIS.NonSquare {
    private static final long serialVersionUID = 2858973491875593956L;

    public static final int ROWS = 2;
    public static final int COLUMNS = 3;

    public double m00;
    public double m01;
    public double m02;
    public double m10;
    public double m11;
    public double m12;

    /**
     * Creates a new matrix filled with only zero values.
     *
     * @param ignore shall always be {@code false} in current version.
     */
    Matrix2x3(boolean ignore) {
    }

    /**
     * Creates a new matrix initialized to the specified values.
     *
     * @param m00 the first matrix element in the first row.
     * @param m01 the second matrix element in the first row.
     * @param m02 the third matrix element in the first row.
     * @param m10 the first matrix element in the second row.
     * @param m11 the second matrix element in the second row.
     * @param m12 the third matrix element in the second row.
     */
    public Matrix2x3(double m00, double m01, double m02,
                     double m10, double m11, double m12) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
    }

    /**
     * Creates a new matrix initialized to the specified values.
     * The length of the given array must be 6 and the values in the same order as the above constructor.
     *
     * @param elements elements of the matrix. Column indices vary fastest.
     * @throws IllegalArgumentException if the given array does not have the expected length.
     * @see #setElements(double[])
     * @see Matrices#create(int, int, double[])
     */
    public Matrix2x3(double[] elements) throws IllegalArgumentException {
        this.setElements(elements);
    }

    /**
     * Creates a new matrix initialized to the same value than the specified one.
     * The specified matrix size must be {@value #ROWS}×{@value #COLUMNS}.
     * This is not verified by this constructor, since it shall be verified by {@link Matrices}.
     *
     * @param matrix the matrix to copy.
     */
    Matrix2x3(Matrix matrix) {
        this.m00 = matrix.getElement(0, 0);
        this.m01 = matrix.getElement(0, 1);
        this.m02 = matrix.getElement(0, 2);
        this.m10 = matrix.getElement(1, 0);
        this.m11 = matrix.getElement(1, 1);
        this.m12 = matrix.getElement(1, 2);
    }

    /**
     * Creates a new matrix filled with zero values.
     *
     * @return a new matrix filled with zero values
     */
    public static Matrix2x3 createZero() {
        return new Matrix2x3(false);
    }

    /**
     * Casts or copies the given matrix to a {@code Matrix3x2} implementation. If the given {@code matrix}
     * is already an instance of {@code Matrix3x2}, then it is returned unchanged. Otherwise this method
     * verifies the matrix size, then copies all elements in a new {@code Matrix3x2} object.
     *
     * @param matrix the matrix to cast or copy, or {@code null}.
     * @return the matrix argument if it can be safely casted (including {@code null} argument),
     * or a copy of the given matrix otherwise.
     * @throws MismatchedMatrixSizeException if the size of the given matrix is not {@value #ROWS}×{@value #COLUMNS}.
     */
    public static Matrix2x3 castOrCopy(Matrix matrix) throws MismatchedMatrixSizeException {
        if (matrix == null || matrix instanceof Matrix2x3) {
            return (Matrix2x3) matrix;
        }
        ensureSizeMatch(ROWS, COLUMNS, matrix);
        return new Matrix2x3(matrix);
    }

    @Override
    public int getNumRow() {
        return ROWS;
    }

    @Override
    public int getNumCol() {
        return COLUMNS;
    }

    @Override
    public double getElement(int row, int column) {
        if (row >= 0 && row < ROWS && column >= 0 && column < COLUMNS) {
            switch (row * COLUMNS + column) {
                case 0:
                    return this.m00;
                case 1:
                    return this.m01;
                case 2:
                    return this.m02;
                case 3:
                    return this.m10;
                case 4:
                    return this.m11;
                case 5:
                    return this.m12;
            }
        }
        throw indexOutOfBounds(row, column);
    }

    @Override
    public void setElement(int row, int column, double value) {
        if (row >= 0 && row < ROWS && column >= 0 && column < COLUMNS) {
            switch (row * COLUMNS + column) {
                case 0:
                    this.m00 = value;
                    return;
                case 1:
                    this.m01 = value;
                    return;
                case 2:
                    this.m02 = value;
                    return;
                case 3:
                    this.m10 = value;
                    return;
                case 4:
                    this.m11 = value;
                    return;
                case 5:
                    this.m12 = value;
                    return;
            }
        }
        throw indexOutOfBounds(row, column);
    }

    @Override
    public double[] getElements() {
        return new double[]{
                this.m00, this.m01, this.m02,
                this.m10, this.m11, this.m12,
        };
    }

    @Override
    public void getElements(final double[] dest) {
        ensureLengthMatch(ROWS * COLUMNS, dest);
        dest[0] = this.m00;
        dest[1] = this.m01;
        dest[2] = this.m02;
        dest[3] = this.m10;
        dest[4] = this.m11;
        dest[5] = this.m12;
    }

    @Override
    public void setElements(double[] elements) {
        ensureLengthMatch(ROWS * COLUMNS, elements);
        this.m00 = elements[0];
        this.m01 = elements[1];
        this.m02 = elements[2];
        this.m10 = elements[3];
        this.m11 = elements[4];
        this.m12 = elements[5];
    }

    @Override
    public Matrix2x3 clone() {
        return (Matrix2x3) super.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object.getClass() == this.getClass()) {
            Matrix2x3 that = (Matrix2x3) object;
            return Numerics.equals(this.m00, that.m00)
                   && Numerics.equals(this.m01, that.m01)
                   && Numerics.equals(this.m02, that.m02)
                   && Numerics.equals(this.m10, that.m10)
                   && Numerics.equals(this.m11, that.m11)
                   && Numerics.equals(this.m12, that.m12);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(serialVersionUID ^
                             (((((Double.doubleToLongBits(this.m00) +
                                  31 * Double.doubleToLongBits(this.m01)) +
                                 31 * Double.doubleToLongBits(this.m02)) +
                                31 * Double.doubleToLongBits(this.m10)) +
                               31 * Double.doubleToLongBits(this.m11)) +
                              31 * Double.doubleToLongBits(this.m12)));
    }
}
