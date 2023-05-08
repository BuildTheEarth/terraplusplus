package net.buildtheearth.terraplusplus.util.math.matrix;

import org.apache.sis.internal.util.Numerics;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.Matrix3;
import org.apache.sis.referencing.operation.matrix.MismatchedMatrixSizeException;
import org.opengis.referencing.operation.Matrix;

/**
 * A {@link Matrix} with {@code 3} rows and {@code 2} columns.
 *
 * <blockquote><pre> ┌         ┐
 * │ {@linkplain #m00} {@linkplain #m01} │
 * │ {@linkplain #m10} {@linkplain #m11} │
 * │ {@linkplain #m20} {@linkplain #m21} │
 * └         ┘</pre></blockquote>
 *
 * @author DaPorkchop_
 * @see Matrix2
 * @see Matrix3
 * @see Matrix2x3
 */
public final class Matrix3x2 extends AbstractMatrixSIS.NonSquare {
    private static final long serialVersionUID = 2858973491875593956L;

    public static final int ROWS = 3;
    public static final int COLUMNS = 2;

    public double m00;
    public double m01;
    public double m10;
    public double m11;
    public double m20;
    public double m21;

    /**
     * Creates a new matrix filled with only zero values.
     *
     * @param ignore shall always be {@code false} in current version.
     */
    Matrix3x2(boolean ignore) {
    }

    /**
     * Creates a new matrix initialized to the specified values.
     *
     * @param m00 the first matrix element in the first row.
     * @param m01 the second matrix element in the first row.
     * @param m10 the first matrix element in the second row.
     * @param m11 the second matrix element in the second row.
     * @param m20 the first matrix element in the third row.
     * @param m21 the second matrix element in the third row.
     */
    public Matrix3x2(double m00, double m01,
                     double m10, double m11,
                     double m20, double m21) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m20 = m20;
        this.m21 = m21;
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
    public Matrix3x2(double[] elements) throws IllegalArgumentException {
        this.setElements(elements);
    }

    /**
     * Creates a new matrix initialized to the same value than the specified one.
     * The specified matrix size must be {@value #ROWS}×{@value #COLUMNS}.
     * This is not verified by this constructor, since it shall be verified by {@link Matrices}.
     *
     * @param matrix the matrix to copy.
     */
    Matrix3x2(Matrix matrix) {
        this.m00 = matrix.getElement(0, 0);
        this.m01 = matrix.getElement(0, 1);
        this.m10 = matrix.getElement(1, 0);
        this.m11 = matrix.getElement(1, 1);
        this.m20 = matrix.getElement(2, 0);
        this.m21 = matrix.getElement(2, 1);
    }

    /**
     * Creates a new matrix filled with zero values.
     *
     * @return a new matrix filled with zero values
     */
    public static Matrix3x2 createZero() {
        return new Matrix3x2(false);
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
    public static Matrix3x2 castOrCopy(Matrix matrix) throws MismatchedMatrixSizeException {
        if (matrix == null || matrix instanceof Matrix3x2) {
            return (Matrix3x2) matrix;
        }
        ensureSizeMatch(ROWS, COLUMNS, matrix);
        return new Matrix3x2(matrix);
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
                    return this.m10;
                case 3:
                    return this.m11;
                case 4:
                    return this.m20;
                case 5:
                    return this.m21;
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
                    this.m10 = value;
                    return;
                case 3:
                    this.m11 = value;
                    return;
                case 4:
                    this.m20 = value;
                    return;
                case 5:
                    this.m21 = value;
                    return;
            }
        }
        throw indexOutOfBounds(row, column);
    }

    @Override
    public double[] getElements() {
        return new double[]{
                this.m00, this.m01,
                this.m10, this.m11,
                this.m20, this.m21,
        };
    }

    @Override
    public void getElements(final double[] dest) {
        ensureLengthMatch(ROWS * COLUMNS, dest);
        dest[0] = this.m00;
        dest[1] = this.m01;
        dest[2] = this.m10;
        dest[3] = this.m11;
        dest[4] = this.m20;
        dest[5] = this.m21;
    }

    @Override
    public void setElements(double[] elements) {
        ensureLengthMatch(ROWS * COLUMNS, elements);
        this.m00 = elements[0];
        this.m01 = elements[1];
        this.m10 = elements[2];
        this.m11 = elements[3];
        this.m20 = elements[4];
        this.m21 = elements[5];
    }

    @Override
    public Matrix3x2 clone() {
        return (Matrix3x2) super.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object.getClass() == this.getClass()) {
            Matrix3x2 that = (Matrix3x2) object;
            return Numerics.equals(this.m00, that.m00)
                   && Numerics.equals(this.m01, that.m01)
                   && Numerics.equals(this.m10, that.m10)
                   && Numerics.equals(this.m11, that.m11)
                   && Numerics.equals(this.m20, that.m20)
                   && Numerics.equals(this.m21, that.m21);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(serialVersionUID ^
                             (((((Double.doubleToLongBits(this.m00) +
                                  31 * Double.doubleToLongBits(this.m01)) +
                                 31 * Double.doubleToLongBits(this.m10)) +
                                31 * Double.doubleToLongBits(this.m11)) +
                               31 * Double.doubleToLongBits(this.m20)) +
                              31 * Double.doubleToLongBits(this.m21)));
    }
}
