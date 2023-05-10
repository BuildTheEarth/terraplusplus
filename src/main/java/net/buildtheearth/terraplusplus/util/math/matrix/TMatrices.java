package net.buildtheearth.terraplusplus.util.math.matrix;

import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.annotation.param.Positive;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.Matrix1;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.Matrix3;
import org.apache.sis.referencing.operation.matrix.Matrix4;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;

import javax.vecmath.Vector3d;

import static net.buildtheearth.terraplusplus.util.TerraUtils.*;

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

    // multiplyFast overloads

    public static Matrix2 multiplyFast(Matrix2 m1, Matrix2 m2) {
        return new Matrix2(
                m1.m00 * m2.m00 + m1.m01 * m2.m10, m1.m00 * m2.m01 + m1.m01 * m2.m11,
                m1.m10 * m2.m00 + m1.m11 * m2.m10, m1.m10 * m2.m01 + m1.m11 * m2.m11);
    }

    public static void multiplyFast(Matrix2 m1, Matrix2 m2, Matrix2 dst) {
        //preload all fields into variables to improve optimization and allow in-place calculation
        double m1m00 = m1.m00;
        double m1m01 = m1.m01;
        double m1m10 = m1.m10;
        double m1m11 = m1.m11;
        
        double m2m00 = m2.m00;
        double m2m01 = m2.m01;
        double m2m10 = m2.m10;
        double m2m11 = m2.m11;
        
        dst.m00 = m1m00 * m2m00 + m1m01 * m2m10;
        dst.m01 = m1m00 * m2m01 + m1m01 * m2m11;
        dst.m10 = m1m10 * m2m00 + m1m11 * m2m10;
        dst.m11 = m1m10 * m2m01 + m1m11 * m2m11;
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

    public static void multiplyFast(Matrix2 m1, Matrix2x3 m2, Matrix2x3 dst) {
        //preload all fields into variables to improve optimization and allow in-place calculation
        double m1m00 = m1.m00;
        double m1m01 = m1.m01;
        double m1m10 = m1.m10;
        double m1m11 = m1.m11;
        
        double m2m00 = m2.m00;
        double m2m01 = m2.m01;
        double m2m02 = m2.m02;
        double m2m10 = m2.m10;
        double m2m11 = m2.m11;
        double m2m12 = m2.m12;
        
        dst.m00 = m1m00 * m2m00 + m1m01 * m2m10;
        dst.m01 = m1m00 * m2m01 + m1m01 * m2m11;
        dst.m02 = m1m00 * m2m02 + m1m01 * m2m12;
        dst.m10 = m1m10 * m2m00 + m1m11 * m2m10;
        dst.m11 = m1m10 * m2m01 + m1m11 * m2m11;
        dst.m12 = m1m10 * m2m02 + m1m11 * m2m12;
    }

    public static void multiplyFast(Matrix3 m1, Matrix3x2 m2, Matrix3x2 dst) {
        //preload all fields into variables to improve optimization and allow in-place calculation
        double m1m00 = m1.m00;
        double m1m01 = m1.m01;
        double m1m02 = m1.m02;
        double m1m10 = m1.m10;
        double m1m11 = m1.m11;
        double m1m12 = m1.m12;
        double m1m20 = m1.m20;
        double m1m21 = m1.m21;
        double m1m22 = m1.m22;
        
        double m2m00 = m2.m00;
        double m2m01 = m2.m01;
        double m2m10 = m2.m10;
        double m2m11 = m2.m11;
        double m2m20 = m2.m20;
        double m2m21 = m2.m21;
        
        dst.m00 = m1m00 * m2m00 + m1m01 * m2m10 + m1m02 * m2m20;
        dst.m01 = m1m00 * m2m01 + m1m01 * m2m11 + m1m02 * m2m21;
        dst.m10 = m1m10 * m2m00 + m1m11 * m2m10 + m1m12 * m2m20;
        dst.m11 = m1m10 * m2m01 + m1m11 * m2m11 + m1m12 * m2m21;
        dst.m20 = m1m20 * m2m00 + m1m21 * m2m10 + m1m22 * m2m20;
        dst.m21 = m1m20 * m2m01 + m1m21 * m2m11 + m1m22 * m2m21;
    }

    public static void multiplyFast(Matrix3x2 m1, Matrix2 m2, Matrix3x2 dst) {
        //preload all fields into variables to improve optimization and allow in-place calculation
        double m1m00 = m1.m00;
        double m1m01 = m1.m01;
        double m1m10 = m1.m10;
        double m1m11 = m1.m11;
        double m1m20 = m1.m20;
        double m1m21 = m1.m21;

        double m2m00 = m2.m00;
        double m2m01 = m2.m01;
        double m2m10 = m2.m10;
        double m2m11 = m2.m11;
        
        dst.m00 = m1m00 * m2m00 + m1m01 * m2m10;
        dst.m01 = m1m00 * m2m01 + m1m01 * m2m11;
        dst.m10 = m1m10 * m2m00 + m1m11 * m2m10;
        dst.m11 = m1m10 * m2m01 + m1m11 * m2m11;
        dst.m20 = m1m20 * m2m00 + m1m21 * m2m10;
        dst.m21 = m1m20 * m2m01 + m1m21 * m2m11;
    }

    public static void multiplyFast(Matrix3 m1, Vector3d v2, Vector3d dst) {
        //preload all fields into variables to improve optimization and allow in-place calculation
        multiplyFast(m1, v2.x, v2.y, v2.z, dst);
    }

    public static void multiplyFast(Matrix3 m1, double x2, double y2, double z2, Vector3d dst) {
        dst.x = m1.m00 * x2 + m1.m01 * y2 + m1.m02 * z2;
        dst.y = m1.m10 * x2 + m1.m11 * y2 + m1.m12 * z2;
        dst.z = m1.m20 * x2 + m1.m21 * y2 + m1.m22 * z2;
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

    public static void scaleFast(Matrix2 m, double f, Matrix2 dst) {
        dst.m00 = m.m00 * f;
        dst.m01 = m.m01 * f;
        dst.m10 = m.m10 * f;
        dst.m11 = m.m11 * f;
    }

    public static double detFast(Matrix2 m) {
        return m.m00 * m.m11 - m.m01 * m.m10;
    }

    public static void invertFast(Matrix2 m, Matrix2 dst) {
        double det = detFast(m);
        double a = m.m00;
        double b = m.m01;
        double c = m.m10;
        double d = m.m11;
        dst.m00 = d / det;
        dst.m01 = -b / det;
        dst.m10 = -c / det;
        dst.m11 = a / det;
    }

    public static void pseudoInvertFast(Matrix2x3 m, Matrix3x2 dst) {
        double a = m.m00;
        double b = m.m01;
        double c = m.m02;
        double d = m.m10;
        double e = m.m11;
        double f = m.m12;
        
        // haha yes:
        // https://www.wolframalpha.com/input?i=pseudoinverse+of+%28%28a%2C+b%2C+c%29%2C%28d%2C+e%2C+f%29%29
        // Simplify[Simplify[Simplify[Simplify[Simplify[Simplify[
        //   {
        //     {
        //       ((d Conjugate[a]+e Conjugate[b]+f Conjugate[c]) Conjugate[d])/(-(b d Conjugate[b d])+a e Conjugate[b d]-c d Conjugate[c d]+a f Conjugate[c d]+b d Conjugate[a e]-a e Conjugate[a e]-c e Conjugate[c e]+b f Conjugate[c e]+c d Conjugate[a f]-a f Conjugate[a f]+c e Conjugate[b f]-b f Conjugate[b f])+(Conjugate[a] (d Conjugate[d]+e Conjugate[e]+f Conjugate[f]))/(b d Conjugate[b d]-a e Conjugate[b d]+c d Conjugate[c d]-a f Conjugate[c d]-b d Conjugate[a e]+a e Conjugate[a e]+c e Conjugate[c e]-b f Conjugate[c e]-c d Conjugate[a f]+a f Conjugate[a f]-c e Conjugate[b f]+b f Conjugate[b f]),
        //       (Conjugate[a] (a Conjugate[d]+b Conjugate[e]+c Conjugate[f]))/(-(b d Conjugate[b d])+a e Conjugate[b d]-c d Conjugate[c d]+a f Conjugate[c d]+b d Conjugate[a e]-a e Conjugate[a e]-c e Conjugate[c e]+b f Conjugate[c e]+c d Conjugate[a f]-a f Conjugate[a f]+c e Conjugate[b f]-b f Conjugate[b f])+((a Conjugate[a]+b Conjugate[b]+c Conjugate[c]) Conjugate[d])/(b d Conjugate[b d]-a e Conjugate[b d]+c d Conjugate[c d]-a f Conjugate[c d]-b d Conjugate[a e]+a e Conjugate[a e]+c e Conjugate[c e]-b f Conjugate[c e]-c d Conjugate[a f]+a f Conjugate[a f]-c e Conjugate[b f]+b f Conjugate[b f])
        //     },
        //     {
        //       ((d Conjugate[a]+e Conjugate[b]+f Conjugate[c]) Conjugate[e])/(-(b d Conjugate[b d])+a e Conjugate[b d]-c d Conjugate[c d]+a f Conjugate[c d]+b d Conjugate[a e]-a e Conjugate[a e]-c e Conjugate[c e]+b f Conjugate[c e]+c d Conjugate[a f]-a f Conjugate[a f]+c e Conjugate[b f]-b f Conjugate[b f])+(Conjugate[b] (d Conjugate[d]+e Conjugate[e]+f Conjugate[f]))/(b d Conjugate[b d]-a e Conjugate[b d]+c d Conjugate[c d]-a f Conjugate[c d]-b d Conjugate[a e]+a e Conjugate[a e]+c e Conjugate[c e]-b f Conjugate[c e]-c d Conjugate[a f]+a f Conjugate[a f]-c e Conjugate[b f]+b f Conjugate[b f]),
        //       (Conjugate[b] (a Conjugate[d]+b Conjugate[e]+c Conjugate[f]))/(-(b d Conjugate[b d])+a e Conjugate[b d]-c d Conjugate[c d]+a f Conjugate[c d]+b d Conjugate[a e]-a e Conjugate[a e]-c e Conjugate[c e]+b f Conjugate[c e]+c d Conjugate[a f]-a f Conjugate[a f]+c e Conjugate[b f]-b f Conjugate[b f])+((a Conjugate[a]+b Conjugate[b]+c Conjugate[c]) Conjugate[e])/(b d Conjugate[b d]-a e Conjugate[b d]+c d Conjugate[c d]-a f Conjugate[c d]-b d Conjugate[a e]+a e Conjugate[a e]+c e Conjugate[c e]-b f Conjugate[c e]-c d Conjugate[a f]+a f Conjugate[a f]-c e Conjugate[b f]+b f Conjugate[b f])
        //     },
        //     {
        //       ((d Conjugate[a]+e Conjugate[b]+f Conjugate[c]) Conjugate[f])/(-(b d Conjugate[b d])+a e Conjugate[b d]-c d Conjugate[c d]+a f Conjugate[c d]+b d Conjugate[a e]-a e Conjugate[a e]-c e Conjugate[c e]+b f Conjugate[c e]+c d Conjugate[a f]-a f Conjugate[a f]+c e Conjugate[b f]-b f Conjugate[b f])+(Conjugate[c] (d Conjugate[d]+e Conjugate[e]+f Conjugate[f]))/(b d Conjugate[b d]-a e Conjugate[b d]+c d Conjugate[c d]-a f Conjugate[c d]-b d Conjugate[a e]+a e Conjugate[a e]+c e Conjugate[c e]-b f Conjugate[c e]-c d Conjugate[a f]+a f Conjugate[a f]-c e Conjugate[b f]+b f Conjugate[b f]),
        //       (Conjugate[c] (a Conjugate[d]+b Conjugate[e]+c Conjugate[f]))/(-(b d Conjugate[b d])+a e Conjugate[b d]-c d Conjugate[c d]+a f Conjugate[c d]+b d Conjugate[a e]-a e Conjugate[a e]-c e Conjugate[c e]+b f Conjugate[c e]+c d Conjugate[a f]-a f Conjugate[a f]+c e Conjugate[b f]-b f Conjugate[b f])+((a Conjugate[a]+b Conjugate[b]+c Conjugate[c]) Conjugate[f])/(b d Conjugate[b d]-a e Conjugate[b d]+c d Conjugate[c d]-a f Conjugate[c d]-b d Conjugate[a e]+a e Conjugate[a e]+c e Conjugate[c e]-b f Conjugate[c e]-c d Conjugate[a f]+a f Conjugate[a f]-c e Conjugate[b f]+b f Conjugate[b f])
        //     }
        //   },
        //   Element[a, Reals]],Element[b, Reals]],Element[c, Reals]],Element[d, Reals]],Element[e, Reals]],Element[f, Reals]]

        double factor = 1.0d / (sq(c) * (sq(d) + sq(e)) - 2.0d * a * c * d * f - 2.0d * b * e * (a * d + c * f) + sq(b) * (sq(d) + sq(f)) + sq(a) * (sq(e) + sq(f)));

        dst.m00 = (a * (e * e + f * f) - b * d * e - c * d * f) * factor;
        dst.m01 = (c * (c * d - a * f) + b * b * d - a * b * e) * factor;
        dst.m10 = (b * (d * d + f * f) - e * a * d - e * c * f) * factor;
        dst.m11 = (c * (c * e - b * f) - a * b * d + a * a * e) * factor;
        dst.m20 = (c * (d * d + e * e) - a * d * f - b * e * f) * factor;
        dst.m21 = (b * (b * f - c * e) - a * c * d + a * a * f) * factor;
    }
}
