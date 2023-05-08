package net.buildtheearth.terraplusplus.util.math.matrix;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.daporkchop.lib.common.util.PorkUtil;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.matrix.MismatchedMatrixSizeException;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.resources.Errors;
import org.opengis.referencing.operation.Matrix;

/**
 * Copies some methods from {@link MatrixSIS} to make them not be package-private.
 *
 * @author DaPorkchop_
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractMatrixSIS extends MatrixSIS {
    private static final long serialVersionUID = -2209509649445570374L;

    /**
     * Ensures that the given array is non-null and has the expected length.
     * This is a convenience method for subclasses constructors.
     *
     * @throws IllegalArgumentException if the given array does not have the expected length.
     */
    protected static void ensureLengthMatch(int expected, @NonNull double[] elements) throws IllegalArgumentException {
        if (elements.length != expected) {
            throw new IllegalArgumentException(Errors.format(Errors.Keys.UnexpectedArrayLength_2, expected, elements.length));
        }
    }

    /**
     * Ensures that the given matrix has the given dimension.
     * This is a convenience method for subclasses.
     */
    protected static void ensureSizeMatch(int numRow, int numCol, Matrix matrix) throws MismatchedMatrixSizeException {
        int otherRow = matrix.getNumRow();
        int otherCol = matrix.getNumCol();
        if (numRow != otherRow || numCol != otherCol) {
            throw new MismatchedMatrixSizeException(Errors.format(Errors.Keys.MismatchedMatrixSize_4, numRow, numCol, otherRow, otherCol));
        }
    }

    /**
     * Ensures that the number of rows of a given matrix matches the given value.
     * This is a convenience method for {@link #multiply(Matrix)} implementations.
     *
     * @param expected the expected number of rows.
     * @param actual   the actual number of rows in the matrix to verify.
     * @param numCol   the number of columns to report in case of errors. This is an arbitrary
     *                 value and have no incidence on the verification performed by this method.
     */
    protected static void ensureNumRowMatch(int expected, int actual, int numCol) {
        if (actual != expected) {
            throw new MismatchedMatrixSizeException(Errors.format(Errors.Keys.MismatchedMatrixSize_4, expected, "⒩", actual, numCol));
        }
    }

    /**
     * Returns an exception for the given indices.
     */
    protected static IndexOutOfBoundsException indexOutOfBounds(int row, int column) {
        return new IndexOutOfBoundsException(Errors.format(Errors.Keys.IndicesOutOfBounds_2, row, column));
    }

    /**
     * Stores all matrix elements in the given flat array. This method does not verify the array length.
     * All subclasses in this {@code org.apache.sis.referencing.operation.matrix} package override this
     * method with a more efficient implementation.
     *
     * @param  dest  the destination array. May be longer than necessary (this happen when the caller needs to
     *               append {@link org.apache.sis.internal.util.DoubleDouble#error} values after the elements).
     * @see MatrixSIS#getElements(double[])
     */
    @SuppressWarnings("JavadocReference")
    public abstract void getElements(final double[] dest);

    /**
     * @author DaPorkchop_
     */
    public static abstract class NonSquare extends AbstractMatrixSIS {
        private static final long serialVersionUID = -4756653951333372707L;

        @Override
        public final boolean isAffine() {
            return false; //non-square matrix
        }

        @Override
        public final boolean isIdentity() {
            return false; //non-square matrix
        }

        @Override
        public final void transpose() {
            throw new UnsupportedOperationException(PorkUtil.className(this));
        }
    }
}
