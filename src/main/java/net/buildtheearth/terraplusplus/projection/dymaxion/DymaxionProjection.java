package net.buildtheearth.terraplusplus.projection.dymaxion;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.GeographicProjectionHelper;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.sis.AbstractOperationMethod;
import net.buildtheearth.terraplusplus.projection.sis.AbstractSISMigratedGeographicProjection;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractFromGeoMathTransform2D;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractToGeoMathTransform2D;
import net.buildtheearth.terraplusplus.util.TerraUtils;
import net.buildtheearth.terraplusplus.util.math.matrix.Matrix2x3;
import net.buildtheearth.terraplusplus.util.math.matrix.Matrix3x2;
import net.buildtheearth.terraplusplus.util.math.matrix.TMatrices;
import org.apache.sis.internal.util.DoubleDouble;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.transform.ContextualParameters;
import org.apache.sis.util.ComparisonMode;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.TransformException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.buildtheearth.terraplusplus.util.TerraUtils.*;

/**
 * Implementation of the Dynmaxion projection.
 * Also known as Airocean or Fuller projection.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Dymaxion_map">Wikipedia's article on the Dynmaxion projection</a>
 */
@JsonDeserialize
public class DymaxionProjection extends AbstractSISMigratedGeographicProjection {

    protected static final double ARC = 2 * Math.asin(Math.sqrt(5 - Math.sqrt(5)) / Math.sqrt(10));
    protected static final double Z = Math.sqrt(5 + 2 * Math.sqrt(5)) / Math.sqrt(15);
    protected static final double EL = Math.sqrt(8) / Math.sqrt(5 + Math.sqrt(5));
    protected static final double EL6 = EL / 6;
    protected static final double DVE = Math.sqrt(3 + Math.sqrt(5)) / Math.sqrt(5 + Math.sqrt(5));
    protected static final double R = -3 * EL6 / DVE;

    /**
     * Number of iterations for Newton's method
     */
    private static final int NEWTON = 5;

    /**
     * This contains the vertices of the icosahedron,
     * identified by their geographic longitude and latitude in degrees.
     * When the class is loaded, a static block below converts all these coordinates
     * to the equivalent spherical coordinates (longitude and colatitude), in radians.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Regular_icosahedron#Spherical_coordinates">Wikipedia</a>
     */
    protected static final double[][] VERTICES = {
            { 10.536199, 64.700000 },
            { -5.245390, 2.300882 },
            { 58.157706, 10.447378 },
            { 122.300000, 39.100000 },
            { -143.478490, 50.103201 },
            { -67.132330, 23.717925 },
            { 36.521510, -50.103200 },
            { 112.867673, -23.717930 },
            { 174.754610, -2.300882 },
            { -121.842290, -10.447350 },
            { -57.700000, -39.100000 },
            { -169.463800, -64.700000 },
    };

    /**
     * Indicates the vertices forming each face of the icosahedron.
     * Each entry refers to the index of a vertex in {@link #VERTICES}
     */
    protected static final int[][] ISO = {
            { 2, 1, 6 },
            { 1, 0, 2 },
            { 0, 1, 5 },
            { 1, 5, 10 },
            { 1, 6, 10 },
            { 7, 2, 6 },
            { 2, 3, 7 },
            { 3, 0, 2 },
            { 0, 3, 4 },
            { 4, 0, 5 }, //9, qubec
            { 5, 4, 9 },
            { 9, 5, 10 },
            { 10, 9, 11 },
            { 11, 6, 10 },
            { 6, 7, 11 },
            { 8, 3, 7 },
            { 8, 3, 4 },
            { 8, 4, 9 },
            { 9, 8, 11 },
            { 7, 8, 11 },
            { 11, 6, 7 }, //child of 14
            { 3, 7, 8 } //child of 15
    };

    protected static final double[][] CENTER_MAP = {
            { -3, 7 },
            { -2, 5 },
            { -1, 7 },
            { 2, 5 },
            { 4, 5 },
            { -4, 1 },
            { -3, -1 },
            { -2, 1 },
            { -1, -1 },
            { 0, 1 },
            { 1, -1 },
            { 2, 1 },
            { 3, -1 },
            { 4, 1 },
            { 5, -1 }, //14, left side, right to be cut
            { -3, -5 },
            { -1, -5 },
            { 1, -5 },
            { 2, -7 },
            { -4, -7 },
            { -5, -5 }, //20, pseudo triangle, child of 14
            { -2, -7 } //21 , pseudo triangle, child of 15
    };

    /**
     * Indicates for each face if it needs to be flipped after projecting
     */
    protected static final boolean[] FLIP_TRIANGLE = {
            true, false, true, false, false,
            true, false, true, false, true, false, true, false, true, false,
            true, true, true, false, false,
            true, false
    };

    /**
     * Indicates for each face if it needs to be flipped after projecting.
     * <p>
     * Each element is {@code -1.0d} if the corresponding element in {@link #FLIP_TRIANGLE} is {@code true}, and {@code 1.0d} otherwise.
     */
    protected static final double[] FLIP_TRIANGLE_FACTOR;

    /**
     * This contains the Cartesian coordinates the centroid
     * of each face of the icosahedron.
     */
    protected static final double[][] CENTROIDS = new double[22][3];

    /**
     * Rotation matrices to move the triangles to the reference coordinates from the original positions.
     * Indexed by the face's indices.
     */
    protected static final double[][][] ROTATION_MATRICES = new double[22][3][3];

    /**
     * Rotation matrices to move the triangles from the reference coordinates to their original positions.
     * Indexed by the face's indices.
     */
    protected static final double[][][] INVERSE_ROTATION_MATRICES = new double[22][3][3];

    protected static final int[] FACE_ON_GRID = {
            -1, -1, 0, 1, 2, -1, -1, 3, -1, 4, -1,
            -1, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            20, 19, 15, 21, 16, -1, 17, 18, -1, -1, -1,
    };

    static {

        for (int i = 0; i < 22; i++) {
            CENTER_MAP[i][0] *= 0.5 * ARC;
            CENTER_MAP[i][1] *= ARC * TerraUtils.ROOT3 / 12;
        }

        // Will contain the list of vertices in Cartesian coordinates
        double[][] verticesCartesian = new double[VERTICES.length][3];

        // Convert the geographic vertices to spherical in radians
        for (int i = 0; i < VERTICES.length; i++) {
            double[] vertexSpherical = TerraUtils.geo2Spherical(VERTICES[i]);
            double[] vertex = TerraUtils.spherical2Cartesian(vertexSpherical);
            verticesCartesian[i] = vertex;
            VERTICES[i] = vertexSpherical;
        }

        for (int i = 0; i < 22; i++) {
            // Vertices of the current face
            double[] vec1 = verticesCartesian[ISO[i][0]];
            double[] vec2 = verticesCartesian[ISO[i][1]];
            double[] vec3 = verticesCartesian[ISO[i][2]];

            // Find the centroid's projection onto the sphere
            double xsum = vec1[0] + vec2[0] + vec3[0];
            double ysum = vec1[1] + vec2[1] + vec3[1];
            double zsum = vec1[2] + vec2[2] + vec3[2];
            double mag = Math.sqrt(xsum * xsum + ysum * ysum + zsum * zsum);
            CENTROIDS[i] = new double[]{ xsum / mag, ysum / mag, zsum / mag };

            double[] centroidSpherical = TerraUtils.cartesian2Spherical(CENTROIDS[i]);
            double centroidLambda = centroidSpherical[0];
            double centroidPhi = centroidSpherical[1];

            double[] vertex = VERTICES[ISO[i][0]];
            double[] v = { vertex[0] - centroidLambda, vertex[1] };
            v = yRot(v, -centroidPhi);

            ROTATION_MATRICES[i] = TerraUtils.produceZYZRotationMatrix(-centroidLambda, -centroidPhi, (Math.PI / 2) - v[0]);
            INVERSE_ROTATION_MATRICES[i] = TerraUtils.produceZYZRotationMatrix(v[0] - (Math.PI / 2), centroidPhi, centroidLambda);
        }

        FLIP_TRIANGLE_FACTOR = new double[FLIP_TRIANGLE.length];
        for (int i = 0; i < FLIP_TRIANGLE.length; i++) {
            FLIP_TRIANGLE_FACTOR[i] = FLIP_TRIANGLE[i] ? -1.0d : 1.0d;
        }
    }

    protected static int findTriangleGrid(double x, double y) {

        //cast equilateral triangles to 45 degrees right triangles (side length of root2)
        double xp = x / ARC;
        double yp = y / (ARC * TerraUtils.ROOT3);

        int row;
        if (yp > -0.25) {
            if (yp < 0.25) { //middle
                row = 1;
            } else if (yp <= 0.75) { //top
                row = 0;
                yp = 0.5 - yp; //translate to middle and flip
            } else {
                return -1;
            }
        } else if (yp >= -0.75) { //bottom
            row = 2;
            yp = -yp - 0.5; //translate to middle and flip
        } else {
            return -1;
        }

        yp += 0.25; //change origin to vertex 4, to allow grids to align

        //rotate coords 45 degrees so left and right sides of the triangle become the x/y axies (also side lengths are now 1)
        double xr = xp - yp;
        double yr = xp + yp;

        //assign a order to what grid along the y=x line it is
        int gx = (int) Math.floor(xr);
        int gy = (int) Math.floor(yr);

        int col = 2 * gx + (gy != gx ? 1 : 0) + 6;

        //out of bounds
        if (col < 0 || col >= 11) {
            return -1;
        }

        return FACE_ON_GRID[row * 11 + col]; //get face at this position
    }

    protected static double[] yRot(double[] spherical, double rot) {
        double[] c = TerraUtils.spherical2Cartesian(spherical);

        double x = c[0];
        c[0] = c[2] * Math.sin(rot) + x * Math.cos(rot);
        c[2] = c[2] * Math.cos(rot) - x * Math.sin(rot);

        double mag = Math.sqrt(c[0] * c[0] + c[1] * c[1] + c[2] * c[2]);
        c[0] /= mag;
        c[1] /= mag;
        c[2] /= mag;

        return new double[]{
                Math.atan2(c[1], c[0]),
                Math.atan2(Math.sqrt(c[0] * c[0] + c[1] * c[1]), c[2])
        };
    }

    /**
     * Finds the face of the icosahedron on which to project a point.
     * In practice, it works by finding the face with the closest centroid to the point.
     *
     * @param vector - position vector as double array of length 3, using Cartesian coordinates
     * @return an integer identifying the face on which to project the point
     */
    protected static int findTriangle(double[] vector) {

        double min = Double.MAX_VALUE;
        int face = 0;

        for (int i = 0; i < 20; i++) {
            double xd = CENTROIDS[i][0] - vector[0];
            double yd = CENTROIDS[i][1] - vector[1];
            double zd = CENTROIDS[i][2] - vector[2];

            double dissq = xd * xd + yd * yd + zd * zd;
            if (dissq < min) {

                if (dissq < 0.1) //TODO: enlarge radius
                {
                    return i;
                }

                face = i;
                min = dissq;
            }
        }

        return face;
    }

    protected static double[] triangleTransformDymaxion(double[] vec) {

        double S = Z / vec[2];

        double xp = S * vec[0];
        double yp = S * vec[1];

        double a = Math.atan((2 * yp / TerraUtils.ROOT3 - EL6) / DVE); //ARC/2 terms cancel
        double b = Math.atan((xp - yp / TerraUtils.ROOT3 - EL6) / DVE);
        double c = Math.atan((-xp - yp / TerraUtils.ROOT3 - EL6) / DVE);

        return new double[]{ 0.5 * (b - c), (2 * a - b - c) / (2 * TerraUtils.ROOT3) };
    }

    protected static Matrix2x3 triangleTransformDymaxionDeriv(double x, double y, double z) {
        Matrix2x3 matrix = Matrix2x3.createZero();

        //double S = Z / z; // (Z / z)
        //double xp = S * x; // (Z / z * x)
        //double yp = S * y; // (Z / z * y)

        //double a = Math.atan((2 * yp / TerraUtils.ROOT3 - EL6) / DVE); // atan((2 * (Z / z * y) / sqrt(3) - L) / D)
        //double b = Math.atan((xp - yp / TerraUtils.ROOT3 - EL6) / DVE); // atan(((Z / z * x) - (Z / z * y) / sqrt(3) - L) / D)
        //double c = Math.atan((-xp - yp / TerraUtils.ROOT3 - EL6) / DVE); // atan((-(Z / z * x) - (Z / z * y) / sqrt(3) - L) / D)

        /*return new double[] {
                // (1 / 2) * (atan(((Z / z * x) - (Z / z * y) / sqrt(3) - L) / D) - atan((-(Z / z * x) - (Z / z * y) / sqrt(3) - L) / D))
                // (1 / 2) * (ArcTan(((Z / z * x) - (Z / z * y) / sqrt(3) - L) / D) - ArcTan((-(Z / z * x) - (Z / z * y) / sqrt(3) - L) / D))
                0.5 * (b - c),
                // (2 * atan((2 * (Z / z * y) / sqrt(3) - L) / D) - atan(((Z / z * x) - (Z / z * y) / sqrt(3) - L) / D) - atan((-(Z / z * x) - (Z / z * y) / sqrt(3) - L) / D)) / (2 * sqrt(3))
                // (2 * ArcTan((2 * (Z / z * y) / sqrt(3) - L) / D) - ArcTan(((Z / z * x) - (Z / z * y) / sqrt(3) - L) / D) - ArcTan((-(Z / z * x) - (Z / z * y) / sqrt(3) - L) / D)) / (2 * sqrt(3))
                (2 * a - b - c) / (2 * TerraUtils.ROOT3)
        };*/

        // https://www.wolframalpha.com/input?i=d%2Fdx+%281%2F2%29+*+%28atan%28%28%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29+-+atan%28%28-%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29%29
        matrix.setElement(0, 0, DVE * Z * z * (
                3.0d / (2.0d * (3.0d * sq(DVE) * sq(z) + sq(ROOT3 * EL6 * z + Z * (ROOT3 * x + y))))
                - 3.0d / (2.0d * (3.0d * sq(DVE) * sq(z) + sq(ROOT3 * EL6 * z - Z * (ROOT3 * x + y))))
        ));

        // https://www.wolframalpha.com/input?i=d%2Fdy+%281%2F2%29+*+%28atan%28%28%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29+-+atan%28%28-%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29%29
        matrix.setElement(0, 1, (-2.0d * ROOT3 * DVE * sq(Z) * x * z * (3.0d * EL6 * z + ROOT3 * Z * y)) / (
                (3.0d * sq(DVE) * sq(z) + sq(ROOT3 * EL6 * z + Z * (ROOT3 * x + y)))
                * (3.0d * sq(DVE) * sq(z) + sq(ROOT3 * EL6 * z - Z * (ROOT3 * x - y)))
        ));

        // https://www.wolframalpha.com/input?i=d%2Fdz+%281%2F2%29+*+%28atan%28%28%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29+-+atan%28%28-%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29%29
        matrix.setElement(0, 2,
                (-Z * x) / (2.0d * DVE * sq(z) * (sq(-EL6 - (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                - (-Z * x) / (2.0d * DVE * sq(z) * (sq(-EL6 + (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                - (-Z * x) / (2.0d * DVE * sq(z) * (sq(-EL6 - (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                + (-Z * x) / (2.0d * DVE * sq(z) * (sq(-EL6 + (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
        );

        // https://www.wolframalpha.com/input?i=d%2Fdx+%282+*+atan%28%282+*+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29+-+atan%28%28%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29+-+atan%28%28-%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29%29+%2F+%282+*+sqrt%283%29%29
        matrix.setElement(1, 0,
                Z / (2.0d * ROOT3 * DVE * z * (sq(-EL6 - (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                - Z / (2.0d * ROOT3 * DVE * z * (sq(-EL6 + (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
        );

        // https://www.wolframalpha.com/input?i=d%2Fdy+%282+*+atan%28%282+*+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29+-+atan%28%28%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29+-+atan%28%28-%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29%29+%2F+%282+*+sqrt%283%29%29
        matrix.setElement(1, 1,
                Z / (6.0d * DVE * z * (sq(-EL6 - (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                + Z / (6.0d * DVE * z * (sq(-EL6 + (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                + (2.0d * Z) / (3.0d * DVE * z * (sq((2.0d * Z * y) / (ROOT3 * z) - EL6) / sq(DVE) + 1.0d))
        );

        // https://www.wolframalpha.com/input?i=d%2Fdz+%282+*+atan%28%282+*+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29+-+atan%28%28%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29+-+atan%28%28-%28%28Z+%2F+z%29+*+x%29+-+%28%28Z+%2F+z%29+*+y%29+%2F+sqrt%283%29+-+L%29+%2F+D%29%29+%2F+%282+*+sqrt%283%29%29
        matrix.setElement(1, 2,
                (Z * x) / (2.0d * ROOT3 * DVE * sq(z) * (sq(-EL6 + (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                - (Z * x) / (2.0d * ROOT3 * DVE * sq(z) * (sq(-EL6 - (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                - (Z * y) / (6.0d * DVE * sq(z) * (sq(-EL6 + (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                - (Z * y) / (6.0d * DVE * sq(z) * (sq(-EL6 - (Z * x) / z - (Z * y) / (ROOT3 * z)) / sq(DVE) + 1.0d))
                - (2.0d * Z * y) / (3.0d * DVE * sq(z) * (sq((2.0d * Z * y) / (ROOT3 * z) - EL6) / sq(DVE) + 1.0d))
        );

        //double m00 = (3 D z Z (3 D^2 z^2 + 3 L^2 z^2 + 2 sqrt(3) L y z Z + Z^2 (3 x^2 + y^2)))/((3 D^2 z^2 + (sqrt(3) L z - Z (sqrt(3) x - y))^2) (3 D^2 z^2 + (sqrt(3) L z + Z (sqrt(3) x + y))^2));
        //double m01 = -(2 sqrt(3) D x z Z^2 (3 L z + sqrt(3) y Z))/((3 D^2 z^2 + 3 L^2 z^2 - 6 L x z Z + 2 sqrt(3) L y z Z + 3 x^2 Z^2 - 2 sqrt(3) x y Z^2 + y^2 Z^2) (3 D^2 z^2 + 3 L^2 z^2 + 6 L x z Z + 2 sqrt(3) L y z Z + 3 x^2 Z^2 + 2 sqrt(3) x y Z^2 + y^2 Z^2));
        //double m02 = -(3 D x Z (3 D^2 z^2 + 3 L^2 z^2 + Z^2 (3 x^2 - y^2)))/((3 D^2 z^2 + 3 L^2 z^2 - 2 L z Z (3 x - sqrt(3) y) + Z^2 (3 x^2 - 2 sqrt(3) x y + y^2)) (3 D^2 z^2 + 3 L^2 z^2 + 2 L z Z (3 x + sqrt(3) y) + Z^2 (3 x^2 + 2 sqrt(3) x y + y^2)));
        //double m10 = -(2 sqrt(3) D x z Z^2 (3 L z + sqrt(3) y Z))/((3 D^2 z^2 + 3 L^2 z^2 - 6 L x z Z + 2 sqrt(3) L y z Z + 3 x^2 Z^2 - 2 sqrt(3) x y Z^2 + y^2 Z^2) (3 D^2 z^2 + 3 L^2 z^2 + 6 L x z Z + 2 sqrt(3) L y z Z + 3 x^2 Z^2 + 2 sqrt(3) x y Z^2 + y^2 Z^2));
        //double m11 = (3 D z Z (9 D^4 z^4 + 3 D^2 z^2 (6 L^2 z^2 + 2 sqrt(3) L y z Z + Z^2 (5 x^2 + 3 y^2)) + 9 L^4 z^4 + 6 sqrt(3) L^3 y z^3 Z - 9 L^2 z^2 Z^2 (x^2 - y^2) - 4 L y z Z^3 (sqrt(3) x - y) (3 x + sqrt(3) y) + 2 Z^4 (3 x^4 + y^4)))/((3 D^2 z^2 + (sqrt(3) L z - 2 y Z)^2) (3 D^2 z^2 + (sqrt(3) L z - Z (sqrt(3) x - y))^2) (3 D^2 z^2 + (sqrt(3) L z + Z (sqrt(3) x + y))^2));
        //double m12 = -(3 D Z (9 D^4 y z^4 + 3 D^2 z^2 (6 L^2 y z^2 - 2 sqrt(3) L z Z (x^2 - y^2) + 3 y Z^2 (x^2 + y^2)) + 9 L^4 y z^4 - 6 sqrt(3) L^3 z^3 Z (x^2 - y^2) + 9 L^2 y z^2 Z^2 (x^2 + y^2) - 4 sqrt(3) L y^2 z Z^3 (3 x^2 - y^2) + 2 y Z^4 (3 x^4 - 4 x^2 y^2 + y^4)))/((3 D^2 z^2 + (sqrt(3) L z - 2 y Z)^2) (3 D^2 z^2 + 3 L^2 z^2 - 2 L z Z (3 x - sqrt(3) y) + Z^2 (3 x^2 - 2 sqrt(3) x y + y^2)) (3 D^2 z^2 + 3 L^2 z^2 + 2 L z Z (3 x + sqrt(3) y) + Z^2 (3 x^2 + 2 sqrt(3) x y + y^2)));

        // Times[Rational[1,2],Plus[Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1]],Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1]]]]
        // Times[Rational[1,2],Plus[Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]],Times[-1,Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]]]]
        // Times[Rational[1,2],Plus[Times[Power[D,-1],Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Plus[Times[-1,x,Power[z,-2],Z],Times[y,Power[z,-2],Z,Power[sqrt[3],-1]]]],Times[-1,Power[D,-1],Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Plus[Times[x,Power[z,-2],Z],Times[y,Power[z,-2],Z,Power[sqrt[3],-1]]]]]]
        // Times[Rational[1,2],Plus[Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1]],Times[-1,Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1]]],Power[sqrt[3],-1]]
        // Times[Rational[1,2],Plus[Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]],Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]],Times[4,Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[2,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]]],Power[sqrt[3],-1]]
        // Times[Rational[1,2],Plus[Times[-1,Power[D,-1],Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Plus[Times[-1,x,Power[z,-2],Z],Times[y,Power[z,-2],Z,Power[sqrt[3],-1]]]],Times[-1,Power[D,-1],Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Plus[Times[x,Power[z,-2],Z],Times[y,Power[z,-2],Z,Power[sqrt[3],-1]]]],Times[-4,Power[D,-1],y,Power[z,-2],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[2,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]]],Power[sqrt[3],-1]]

        //System.out.println(mathematicaFullFormToJava("Times[Rational[1,2],Plus[Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1]],Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1]]]]"));
        //System.out.println(mathematicaFullFormToJava("Times[Rational[1,2],Plus[Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]],Times[-1,Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]]]]"));
        //System.out.println(mathematicaFullFormToJava("Times[Rational[1,2],Plus[Times[Power[D,-1],Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Plus[Times[-1,x,Power[z,-2],Z],Times[y,Power[z,-2],Z,Power[sqrt[3],-1]]]],Times[-1,Power[D,-1],Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Plus[Times[x,Power[z,-2],Z],Times[y,Power[z,-2],Z,Power[sqrt[3],-1]]]]]]"));
        //System.out.println(mathematicaFullFormToJava("Times[Rational[1,2],Plus[Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1]],Times[-1,Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1]]],Power[sqrt[3],-1]]"));
        //System.out.println(mathematicaFullFormToJava("Times[Rational[1,2],Plus[Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]],Times[Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]],Times[4,Power[D,-1],Power[z,-1],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[2,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]]],Power[sqrt[3],-1]]"));
        //System.out.println(mathematicaFullFormToJava("Times[Rational[1,2],Plus[Times[-1,Power[D,-1],Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Plus[Times[-1,x,Power[z,-2],Z],Times[y,Power[z,-2],Z,Power[sqrt[3],-1]]]],Times[-1,Power[D,-1],Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[-1,x,Power[z,-1],Z],Times[-1,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Plus[Times[x,Power[z,-2],Z],Times[y,Power[z,-2],Z,Power[sqrt[3],-1]]]],Times[-4,Power[D,-1],y,Power[z,-2],Z,Power[Plus[1,Times[Power[D,-2],Power[Plus[Times[-1,L],Times[2,y,Power[z,-1],Z,Power[sqrt[3],-1]]],2]]],-1],Power[sqrt[3],-1]]],Power[sqrt[3],-1]]"));

        matrix.m00 = ((1.0d / 2.0d) * ((Math.pow(DVE, -1.0d) * Math.pow(z, -1.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (-1.0d * x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d)) + (Math.pow(DVE, -1.0d) * Math.pow(z, -1.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d))));
        matrix.m01 = ((1.0d / 2.0d) * ((Math.pow(DVE, -1.0d) * Math.pow(z, -1.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (-1.0d * x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * Math.pow(ROOT3, -1.0d)) + (-1.0d * Math.pow(DVE, -1.0d) * Math.pow(z, -1.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * Math.pow(ROOT3, -1.0d))));
        matrix.m02 = ((1.0d / 2.0d) * ((Math.pow(DVE, -1.0d) * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * ((-1.0d * x * Math.pow(z, -2.0d) * Z) + (y * Math.pow(z, -2.0d) * Z * Math.pow(ROOT3, -1.0d)))) + (-1.0d * Math.pow(DVE, -1.0d) * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (-1.0d * x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * ((x * Math.pow(z, -2.0d) * Z) + (y * Math.pow(z, -2.0d) * Z * Math.pow(ROOT3, -1.0d))))));
        matrix.m10 = ((1.0d / 2.0d) * ((Math.pow(DVE, -1.0d) * Math.pow(z, -1.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (-1.0d * x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d)) + (-1.0d * Math.pow(DVE, -1.0d) * Math.pow(z, -1.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d))) * Math.pow(ROOT3, -1.0d));
        matrix.m11 = ((1.0d / 2.0d) * ((Math.pow(DVE, -1.0d) * Math.pow(z, -1.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (-1.0d * x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * Math.pow(ROOT3, -1.0d)) + (Math.pow(DVE, -1.0d) * Math.pow(z, -1.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * Math.pow(ROOT3, -1.0d)) + (4.0d * Math.pow(DVE, -1.0d) * Math.pow(z, -1.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (2.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * Math.pow(ROOT3, -1.0d))) * Math.pow(ROOT3, -1.0d));
        matrix.m12 = ((1.0d / 2.0d) * ((-1.0d * Math.pow(DVE, -1.0d) * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * ((-1.0d * x * Math.pow(z, -2.0d) * Z) + (y * Math.pow(z, -2.0d) * Z * Math.pow(ROOT3, -1.0d)))) + (-1.0d * Math.pow(DVE, -1.0d) * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (-1.0d * x * Math.pow(z, -1.0d) * Z) + (-1.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * ((x * Math.pow(z, -2.0d) * Z) + (y * Math.pow(z, -2.0d) * Z * Math.pow(ROOT3, -1.0d)))) + (-4.0d * Math.pow(DVE, -1.0d) * y * Math.pow(z, -2.0d) * Z * Math.pow((1.0d + (Math.pow(DVE, -2.0d) * Math.pow(((-1.0d * EL6) + (2.0d * y * Math.pow(z, -1.0d) * Z * Math.pow(ROOT3, -1.0d))), 2.0d))), -1.0d) * Math.pow(ROOT3, -1.0d))) * Math.pow(ROOT3, -1.0d));

        return matrix;
    }

    public static String mathematicaFullFormToJava(String fullForm) {
        fullForm = fullForm.trim();

        try {
            return String.format("%.1fd", Double.parseDouble(fullForm));
        } catch (NumberFormatException e) {
            // not a numeric literal
        }

        switch (fullForm) {
            case "x":
            case "y":
            case "z":
            case "Z":
                return fullForm;
            case "D":
                return "DVE";
            case "L":
                return "EL6";
        }

        int i = fullForm.indexOf('[');
        String func = fullForm.substring(0, i);

        List<String> operands = new ArrayList<>();

        LOOP:
        for (int depth = 0, lastOperandStart = ++i; ; i++) {
            switch (fullForm.charAt(i)) {
                case '[':
                    depth++;
                    break;
                case ']':
                    if (depth-- == 0) {
                        operands.add(fullForm.substring(lastOperandStart, i));
                        break LOOP;
                    }
                    break;
                case ',':
                    if (depth == 0) {
                        operands.add(fullForm.substring(lastOperandStart, i));
                        lastOperandStart = i + 1;
                    }
                    break;
            }
        }

        switch (func) {
            case "Plus":
                return operands.stream().map(DymaxionProjection::mathematicaFullFormToJava).collect(Collectors.joining(" + ", "(", ")"));
            case "Times": {
                String prefix = "(";
                if ("-1".equals(operands.get(0))) {
                    operands.remove(0);
                    prefix = "-(";
                }
                return operands.stream().map(DymaxionProjection::mathematicaFullFormToJava).collect(Collectors.joining(" * ", prefix, ")"));
            }
            case "Rational":
                assert operands.size() == 2 : fullForm;
                return '(' + mathematicaFullFormToJava(operands.get(0)) + " / " + mathematicaFullFormToJava(operands.get(1)) + ')';
            case "Power":
                assert operands.size() == 2 : fullForm;
                return "Math.pow(" + mathematicaFullFormToJava(operands.get(0)) + ", " + mathematicaFullFormToJava(operands.get(1)) + ')';
            case "sqrt":
                assert operands.size() == 1 : fullForm;
                return "ROOT" + operands.get(0);
        }

        throw new IllegalArgumentException(fullForm);
    }

    protected double[] triangleTransform(double[] vec) {
        return triangleTransformDymaxion(vec);
    }

    protected static double[] inverseTriangleTransformNewton(double xpp, double ypp) {

        //a & b are linearly related to c, so using the tan of sum formula we know: tan(c+off) = (tanc + tanoff)/(1-tanc*tanoff)
        double tanaoff = Math.tan(TerraUtils.ROOT3 * ypp + xpp); // a = c + root3*y'' + x''
        double tanboff = Math.tan(2 * xpp); // b = c + 2x''

        double anumer = tanaoff * tanaoff + 1;
        double bnumer = tanboff * tanboff + 1;

        //we will be solving for tanc, starting at t=0, tan(0) = 0
        double tana = tanaoff;
        double tanb = tanboff;
        double tanc = 0;

        double adenom = 1;
        double bdenom = 1;

        //double fp = anumer + bnumer + 1; //derivative relative to tanc

        //int i = newton;
        for (int i = 0; i < NEWTON; i++) {
            double f = tana + tanb + tanc - R; //R = tana + tanb + tanc
            double fp = anumer * adenom * adenom + bnumer * bdenom * bdenom + 1; //derivative relative to tanc

            //TODO: fp could be simplified on first loop: 1 + anumer + bnumer

            tanc -= f / fp;

            adenom = 1 / (1 - tanc * tanaoff);
            bdenom = 1 / (1 - tanc * tanboff);

            tana = (tanc + tanaoff) * adenom;
            tanb = (tanc + tanboff) * bdenom;
        }

        //simple reversal algebra based on tan values
        double yp = TerraUtils.ROOT3 * (DVE * tana + EL6) / 2;
        double xp = DVE * tanb + yp / TerraUtils.ROOT3 + EL6;

        //x = z*xp/Z, y = z*yp/Z, x^2 + y^2 + z^2 = 1
        double xpoZ = xp / Z;
        double ypoZ = yp / Z;

        double z = 1 / Math.sqrt(1 + xpoZ * xpoZ + ypoZ * ypoZ);

        return new double[]{ z * xpoZ, z * ypoZ, z };
    }

    protected double[] inverseTriangleTransform(double x, double y) {
        return inverseTriangleTransformNewton(x, y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {

        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);

        double[] vector = TerraUtils.spherical2Cartesian(TerraUtils.geo2Spherical(new double[]{ longitude, latitude }));

        int face = findTriangle(vector);

        //apply rotation matrix (move triangle onto template triangle)
        double[] pvec = TerraUtils.matVecProdD(ROTATION_MATRICES[face], vector);
        double[] projectedVec = this.triangleTransform(pvec);

        //flip triangle to correct orientation
        if (FLIP_TRIANGLE[face]) {
            projectedVec[0] = -projectedVec[0];
            projectedVec[1] = -projectedVec[1];
        }

        vector[0] = projectedVec[0];
        //deal with special snowflakes (child faces 20, 21)
        if (((face == 15 && vector[0] > projectedVec[1] * TerraUtils.ROOT3) || face == 14) && vector[0] > 0) {
            projectedVec[0] = 0.5 * vector[0] - 0.5 * TerraUtils.ROOT3 * projectedVec[1];
            projectedVec[1] = 0.5 * TerraUtils.ROOT3 * vector[0] + 0.5 * projectedVec[1];
            face += 6; //shift 14->20 & 15->21
        }

        projectedVec[0] += CENTER_MAP[face][0];
        projectedVec[1] += CENTER_MAP[face][1];

        return projectedVec;
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        int face = findTriangleGrid(x, y);

        if (face == -1) {
            throw OutOfProjectionBoundsException.get();
        }

        x -= CENTER_MAP[face][0];
        y -= CENTER_MAP[face][1];

        //deal with bounds of special snowflakes
        switch (face) {
            case 14:
                if (x > 0) {
                    throw OutOfProjectionBoundsException.get();
                }
                break;
            case 20:
                if (-y * TerraUtils.ROOT3 > x) {
                    throw OutOfProjectionBoundsException.get();
                }
                break;
            case 15:
                if (x > 0 && x > y * TerraUtils.ROOT3) {
                    throw OutOfProjectionBoundsException.get();
                }
                break;
            case 21:
                if (x < 0 || -y * TerraUtils.ROOT3 > x) {
                    throw OutOfProjectionBoundsException.get();
                }
                break;
        }

        //flip triangle to upright orientation (if not already)
        if (FLIP_TRIANGLE[face]) {
            x = -x;
            y = -y;
        }

        //invert triangle transform
        double[] c = this.inverseTriangleTransform(x, y);
        x = c[0];
        y = c[1];
        double z = c[2];

        double[] vec = { x, y, z };
        //apply inverse rotation matrix (move triangle from template triangle to correct position on globe)
        double[] vecp = TerraUtils.matVecProdD(INVERSE_ROTATION_MATRICES[face], vec);

        //convert back to geo coordinates
        return TerraUtils.spherical2Geo(TerraUtils.cartesian2Spherical(vecp));
    }

    @Override
    public double[] bounds() {
        return new double[]{ -3 * ARC, -0.75 * ARC * TerraUtils.ROOT3, 2.5 * ARC, 0.75 * ARC * TerraUtils.ROOT3 };
    }

    @Override
    public boolean upright() {
        return false;
    }

    @Override
    public String toString() {
        return "Dymaxion";
    }

    public static final class OperationMethod extends AbstractOperationMethod.ForLegacyProjection {
        public OperationMethod() {
            super("Dymaxion");
        }

        @Override
        protected AbstractFromGeoMathTransform2D createBaseTransform(ParameterValueGroup parameters) throws InvalidParameterNameException, ParameterNotFoundException, InvalidParameterValueException {
            return new FromGeo(parameters);
        }
    }

    private static final class FromGeo extends AbstractFromGeoMathTransform2D {
        public FromGeo(@NonNull ParameterValueGroup parameters) {
            super(parameters, new ToGeo(parameters));
        }

        @Override
        protected void configureMatrices(ContextualParameters contextualParameters, MatrixSIS normalize, MatrixSIS denormalize) {
            //TerraUtils.geo2Spherical()
            normalize.convertAfter(0, DoubleDouble.createDegreesToRadians(), null);
            normalize.convertAfter(1, -1L, 90.0d); //90 - geo[1]
            normalize.convertAfter(1, DoubleDouble.createDegreesToRadians(), null);
        }

        @Override
        public Matrix transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
            //there is no bounds checking here!

            double[] lonLat = Arrays.copyOfRange(srcPts, srcOff, srcOff + 2);

            double[] vector = TerraUtils.spherical2Cartesian(lonLat);
            final int origFace = findTriangle(vector);

            //apply rotation matrix (move triangle onto template triangle)
            double[] rotatedVec = TerraUtils.matVecProdD(ROTATION_MATRICES[origFace], vector);
            double[] projectedVec = triangleTransformDymaxion(rotatedVec);

            //flip triangle to correct orientation
            final double origProjectedX = projectedVec[0] * FLIP_TRIANGLE_FACTOR[origFace];
            final double origProjectedY = projectedVec[1] * FLIP_TRIANGLE_FACTOR[origFace];

            double effectiveProjectedX = origProjectedX;
            double effectiveProjectedY = origProjectedY;
            int effectiveOffsetFace = origFace;

            //deal with special snowflakes (child faces 20, 21)
            if (((origFace == 15 && origProjectedX > ROOT3 * origProjectedY) || origFace == 14) && origProjectedX > 0) {
                effectiveProjectedX = 0.5d * origProjectedX - 0.5d * ROOT3 * origProjectedY;
                effectiveProjectedY = 0.5d * ROOT3 * origProjectedX + 0.5d * origProjectedY;
                effectiveOffsetFace += 6; //shift 14->20 & 15->21
            }

            if (dstPts != null) {
                dstPts[dstOff + 0] = effectiveProjectedX + CENTER_MAP[effectiveOffsetFace][0];
                dstPts[dstOff + 1] = effectiveProjectedY + CENTER_MAP[effectiveOffsetFace][1];
            }
            if (!derivate) {
                return null;
            }

            Matrix3x2 spherical2CartesianDerivative = TerraUtils.spherical2CartesianDerivative(lonLat[0], lonLat[1]);
            Matrix3x2 spherical2CartesianRotatedDerivative = Matrix3x2.castOrCopy(TMatrices.multiplyFast(TerraUtils.matrixToSIS(ROTATION_MATRICES[origFace]), spherical2CartesianDerivative));
            Matrix2x3 triangleTransformDerivative = triangleTransformDymaxionDeriv(rotatedVec[0], rotatedVec[1], rotatedVec[2]);
            Matrix2 spherical2triangleDerivative = TMatrices.multiplyFast(triangleTransformDerivative, spherical2CartesianRotatedDerivative);

            assert Matrices.equals(TMatrices.multiplyFast(triangleTransformDerivative, spherical2CartesianRotatedDerivative), Matrices.multiply(triangleTransformDerivative, spherical2CartesianRotatedDerivative), ComparisonMode.APPROXIMATE);

            //flip triangle to correct orientation
            TMatrices.scaleFast(spherical2triangleDerivative, FLIP_TRIANGLE_FACTOR[origFace], spherical2triangleDerivative);

            //deal with special snowflakes (child faces 20, 21)
            if (((origFace == 15 && origProjectedX > ROOT3 * origProjectedY) || origFace == 14) && origProjectedX > 0) {
                // https://www.wolframalpha.com/input?i=d%2Fdx+%281%2F2%29*x+-+%281%2F2%29*sqrt%283%29*y
                double d00 = 0.5d;

                // https://www.wolframalpha.com/input?i=d%2Fdy+%281%2F2%29*x+-+%281%2F2%29*sqrt%283%29*y
                double d01 = -0.5d * ROOT3;

                // https://www.wolframalpha.com/input?i=d%2Fdx+%281%2F2%29*sqrt%283%29*x+-+%281%2F2%29*y
                double d10 = 0.5d * ROOT3;

                // https://www.wolframalpha.com/input?i=d%2Fdy+%281%2F2%29*sqrt%283%29*x+-+%281%2F2%29*y
                double d11 = 0.5d;

                //spherical2triangleDerivative.m00 *= d00;
                //spherical2triangleDerivative.m01 *= d01;
                //spherical2triangleDerivative.m10 *= d10;
                //spherical2triangleDerivative.m11 *= d11;

                Matrix2 specialFactor = new Matrix2(d00, d01, d10, d11);
                spherical2triangleDerivative = TMatrices.multiplyFast(specialFactor, spherical2triangleDerivative);
            }

            return spherical2triangleDerivative;

            //TODO: compute this accurately
            //return GeographicProjectionHelper.defaultDerivative(new DymaxionProjection(), Math.toDegrees(lonLat[0]), 90d - Math.toDegrees(lonLat[1]), true);
        }
    }

    private static final class ToGeo extends AbstractToGeoMathTransform2D {
        public ToGeo(@NonNull ParameterValueGroup parameters) {
            super(parameters);
        }

        @Override
        public Matrix transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
            final double origX = srcPts[srcOff + 0];
            final double origY = srcPts[srcOff + 1];

            double x = origX;
            double y = origY;

            int face = findTriangleGrid(x, y);
            if (face < 0) {
                throw OutOfProjectionBoundsException.get();
            }
            x -= CENTER_MAP[face][0];
            y -= CENTER_MAP[face][1];

            //deal with bounds of special snowflakes
            switch (face) {
                case 14:
                    if (x > 0) {
                        throw OutOfProjectionBoundsException.get();
                    }
                    break;
                case 20:
                    if (-y * TerraUtils.ROOT3 > x) {
                        throw OutOfProjectionBoundsException.get();
                    }
                    break;
                case 15:
                    if (x > 0 && x > y * TerraUtils.ROOT3) {
                        throw OutOfProjectionBoundsException.get();
                    }
                    break;
                case 21:
                    if (x < 0 || -y * TerraUtils.ROOT3 > x) {
                        throw OutOfProjectionBoundsException.get();
                    }
                    break;
            }

            //flip triangle to upright orientation (if not already)
            if (FLIP_TRIANGLE[face]) {
                x = -x;
                y = -y;
            }

            //invert triangle transform
            double[] c = inverseTriangleTransformNewton(x, y);
            x = c[0];
            y = c[1];
            double z = c[2];

            double[] vec = { x, y, z };
            //apply inverse rotation matrix (move triangle from template triangle to correct position on globe)
            double[] vecp = TerraUtils.matVecProdD(INVERSE_ROTATION_MATRICES[face], vec);

            //convert back to geo coordinates
            double[] vecs = TerraUtils.cartesian2Spherical(vecp);

            //spherical -> geographic conversion is handled afterwards by the affine transform

            if (dstPts != null) {
                dstPts[dstOff + 0] = vecs[0];
                dstPts[dstOff + 1] = vecs[1];
            }
            if (!derivate) {
                return null;
            }

            //TODO: compute this accurately
            return GeographicProjectionHelper.defaultDerivative(new DymaxionProjection(), origX, origY, false);
        }
    }
}