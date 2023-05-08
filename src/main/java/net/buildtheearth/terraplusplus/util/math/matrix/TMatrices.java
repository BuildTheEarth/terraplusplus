package net.buildtheearth.terraplusplus.util.math.matrix;

import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.annotation.param.Positive;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.Matrix1;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.Matrix4;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;

/**
 * Like {@link Matrices}, but better :)
 *
 * @author DaPorkchop_
 * @see Matrices
 */
@UtilityClass
public class TMatrices {
    /**
     * Creates a matrix of size {@code numRow} × {@code numCol} filled with zero values.
     * This constructor is convenient when the caller wants to initialize the matrix elements himself.
     * <p>
     * <div class="note"><b>Implementation note:</b>
     * For {@code numRow} == {@code numCol} with a value between
     * {@value org.apache.sis.referencing.operation.matrix.Matrix1#SIZE} and
     * {@value org.apache.sis.referencing.operation.matrix.Matrix4#SIZE} inclusive, the matrix
     * is guaranteed to be an instance of one of {@link Matrix1} … {@link Matrix4} subtypes.</div>
     *
     * @param numRow for a math transform, this is the number of {@linkplain MathTransform#getTargetDimensions() target dimensions} + 1.
     * @param numCol for a math transform, this is the number of {@linkplain MathTransform#getSourceDimensions() source dimensions} + 1.
     * @return a matrix of the given size with only zero values.
     * @see Matrices#createZero(int, int)
     */
    public static MatrixSIS createZero(@Positive int numRow, @Positive int numCol) {
        if (numRow == numCol) { //fast case for square matrices
            return Matrices.createZero(numRow, numCol);
        } else if (numRow == 2 && numCol == 3) {
            return Matrix2x3.createZero();
        } else if (numRow == 3 && numCol == 2) {
            return Matrix3x2.createZero();
        } else {
            return Matrices.createZero(numRow, numCol);
        }
    }

    public static MatrixSIS multiplyExact(Matrix m1, Matrix m2) {
        return Matrices.multiply(m1, m2);
    }

    public static MatrixSIS multiplyFast(Matrix m1, Matrix m2) {
        return Matrices.multiply(m1, m2);
    }

    // multiplyFast overloads

    public static Matrix2 multiplyFast(Matrix2 m1, Matrix2 m2) {
        return new Matrix2(
                m1.m00 * m2.m00 + m1.m01 * m2.m10, m1.m00 * m2.m01 + m1.m01 * m2.m11,
                m1.m10 * m2.m00 + m1.m11 * m2.m10, m1.m10 * m2.m01 + m1.m11 * m2.m11);
    }

    public static void multiplyFast(Matrix2 m1, Matrix2 m2, Matrix2 dst) {
        dst.m00 = m1.m00 * m2.m00 + m1.m01 * m2.m10;
        dst.m01 = m1.m00 * m2.m01 + m1.m01 * m2.m11;
        dst.m10 = m1.m10 * m2.m00 + m1.m11 * m2.m10;
        dst.m11 = m1.m10 * m2.m01 + m1.m11 * m2.m11;
    }

    public static Matrix2 multiplyFast(Matrix2x3 m1, Matrix3x2 m2) {
        return new Matrix2(
                m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20, m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21,
                m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20, m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21);
    }

    public static void multiplyFast(Matrix2x3 m1, Matrix3x2 m2, Matrix2 dst) {
        dst.m00 = m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20;
        dst.m01 = m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21;
        dst.m10 = m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20;
        dst.m11 = m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21;
    }

    public static MatrixSIS scaleFast(Matrix m, double f) {
        MatrixSIS dst = m instanceof MatrixSIS ? ((MatrixSIS) m).clone() : createZero(m.getNumRow(), m.getNumCol());
        scaleFast(dst, f, dst);
        return dst;
    }

    public static void scaleFast(Matrix m, double f, Matrix dst) {
        int numRow = m.getNumRow();
        int numCol = m.getNumCol();
        AbstractMatrixSIS.ensureSizeMatch(numRow, numCol, dst);

        for (int row = 0; row < numRow; row++) {
            for (int col = 0; col < numCol; col++) {
                dst.setElement(row, col, m.getElement(row, col) * f);
            }
        }
    }

    // scaleFast overloads

    public static Matrix2 scaleFast(Matrix2 m, double f) {
        return new Matrix2(
                m.m00 * f, m.m01 * f,
                m.m10 * f, m.m11 * f);
    }

    public static void scaleFast(Matrix2 m, double f, Matrix2 dst) {
        dst.m00 = m.m00 * f;
        dst.m01 = m.m01 * f;
        dst.m10 = m.m10 * f;
        dst.m11 = m.m11 * f;
    }
}
