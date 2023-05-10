package net.buildtheearth.terraplusplus.projection.dymaxion;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.sis.AbstractOperationMethod;
import net.buildtheearth.terraplusplus.projection.sis.AbstractSISMigratedGeographicProjection;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractFromGeoMathTransform2D;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractToGeoMathTransform2D;
import net.buildtheearth.terraplusplus.util.TerraUtils;
import net.buildtheearth.terraplusplus.util.math.matrix.Matrix2x3;
import net.buildtheearth.terraplusplus.util.math.matrix.Matrix3x2;
import net.buildtheearth.terraplusplus.util.math.matrix.TMatrices;
import net.daporkchop.lib.common.reference.cache.Cached;
import org.apache.sis.internal.util.DoubleDouble;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.Matrix3;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.transform.ContextualParameters;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.TransformException;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
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
    private static final double[][] VERTICES = {
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
    private static final int[][] ISO = {
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

    private static final Vector2d[] CENTER_MAP = {
            new Vector2d(-3, 7),
            new Vector2d(-2, 5),
            new Vector2d(-1, 7),
            new Vector2d(2, 5),
            new Vector2d(4, 5),
            new Vector2d(-4, 1),
            new Vector2d(-3, -1),
            new Vector2d(-2, 1),
            new Vector2d(-1, -1),
            new Vector2d(0, 1),
            new Vector2d(1, -1),
            new Vector2d(2, 1),
            new Vector2d(3, -1),
            new Vector2d(4, 1),
            new Vector2d(5, -1), //14, left side, right to be cut
            new Vector2d(-3, -5),
            new Vector2d(-1, -5),
            new Vector2d(1, -5),
            new Vector2d(2, -7),
            new Vector2d(-4, -7),
            new Vector2d(-5, -5), //20, pseudo triangle, child of 14
            new Vector2d(-2, -7), //21 , pseudo triangle, child of 15
    };

    /**
     * Indicates for each face if it needs to be flipped after projecting
     */
    private static final boolean[] FLIP_TRIANGLE = {
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
    private static final double[] FLIP_TRIANGLE_FACTOR;

    /**
     * This contains the Cartesian coordinates the centroid
     * of each face of the icosahedron.
     */
    protected static final Vector3d[] CENTROIDS = new Vector3d[22];

    /**
     * Rotation matrices to move the triangles to the reference coordinates from the original positions.
     * Indexed by the face's indices.
     */
    protected static final Matrix3[] ROTATION_MATRICES = new Matrix3[22];

    /**
     * Rotation matrices to move the triangles from the reference coordinates to their original positions.
     * Indexed by the face's indices.
     */
    protected static final Matrix3[] INVERSE_ROTATION_MATRICES = new Matrix3[22];

    protected static final int[] FACE_ON_GRID = {
            -1, -1, 0, 1, 2, -1, -1, 3, -1, 4, -1,
            -1, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            20, 19, 15, 21, 16, -1, 17, 18, -1, -1, -1,
    };

    static {
        for (int i = 0; i < 22; i++) {
            CENTER_MAP[i].x *= 0.5d * ARC;
            CENTER_MAP[i].y *= ARC * ROOT3 / 12.0d;
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
            CENTROIDS[i] = new Vector3d(xsum / mag, ysum / mag, zsum / mag);

            Vector2d centroidSpherical = TerraUtils.cartesian2Spherical(CENTROIDS[i]);
            double centroidLambda = centroidSpherical.x;
            double centroidPhi = centroidSpherical.y;

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
     * @param x - the X coordinate, in Cartesian coordinates
     * @param y - the Y coordinate, in Cartesian coordinates
     * @param z - the Z coordinate, in Cartesian coordinates
     * @return an integer identifying the face on which to project the point
     */
    protected static int findTriangle(double x, double y, double z) {
        double min = Double.MAX_VALUE;
        int face = 0;

        for (int i = 0; i < 20; i++) {
            double xd = CENTROIDS[i].x - x;
            double yd = CENTROIDS[i].y - y;
            double zd = CENTROIDS[i].z - z;

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

    protected static int findTriangle(Vector3d cartesian) {
        return findTriangle(cartesian.x, cartesian.y, cartesian.z);
    }

    protected static void triangleTransformDymaxion(double x, double y, double z, Vector2d dst) {
        double S = Z / z;

        double xp = S * x;
        double yp = S * y;

        double a = Math.atan((2 * yp / TerraUtils.ROOT3 - EL6) / DVE); //ARC/2 terms cancel
        double b = Math.atan((xp - yp / TerraUtils.ROOT3 - EL6) / DVE);
        double c = Math.atan((-xp - yp / TerraUtils.ROOT3 - EL6) / DVE);

        dst.x = 0.5 * (b - c);
        dst.y = (2 * a - b - c) / (2 * TerraUtils.ROOT3);
    }

    protected static void triangleTransformDymaxion(Vector3d rotated, Vector2d dst) {
        triangleTransformDymaxion(rotated.x, rotated.y, rotated.z, dst);
    }

    protected static void triangleTransformDymaxionDerivative(double x, double y, double z, Matrix2x3 dst) {
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
        //System.exit(0);

        double d0 = sq(-EL6 - x / z * Z - Z / ROOT3 * y / z);
        double d1 = sq(-EL6 + x / z * Z - Z / ROOT3 * y / z);
        double d2 = sq(-EL6 + 2.0d * Z / ROOT3 * y / z);

        double v0 = z + z * d0 / (DVE * DVE);
        double v1 = z + z * d1 / (DVE * DVE);
        double v2 = Z / DVE / v0;
        double v3 = Z / DVE / v1;

        double v4 = Z / ROOT3 * y / sq(z);
        double v6 = 1.0d / DVE / (1.0d + d1 / (DVE * DVE)) * (-Z * x / sq(z) + v4);
        double v7 = 1.0d / DVE / (1.0d + d0 / (DVE * DVE)) * (Z * x / sq(z) + v4);

        dst.m00 = 0.5d * (v2 + v3);
        dst.m01 = 0.5d * (Z / DVE / ROOT3 / v0 - Z / DVE / ROOT3 / v1);

        dst.m02 = 0.5d * (v6 - v7);
        dst.m10 = 0.5d / ROOT3 * (v2 - v3);
        dst.m11 = 0.5d / ROOT3 * (Z / DVE / ROOT3 / v0 + Z / DVE / ROOT3 / v1 + 4.0d / DVE * Z / ROOT3 / z / (1.0d + 1.0d / (DVE * DVE) * d2));
        dst.m12 = 0.5d / ROOT3 * (-v6 - v7 + -4.0d / DVE * Z / ROOT3 * y / (sq(z) + sq(z) / (DVE * DVE) * d2));
    }

    protected static void triangleTransformDymaxionDerivative(Vector3d rotated, Matrix2x3 dst) {
        triangleTransformDymaxionDerivative(rotated.x, rotated.y, rotated.z, dst);
    }

    /*public static String mathematicaFullFormToJava(String fullForm) {
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
                try {
                    int pow = Integer.parseInt(operands.get(1));
                    String prefix = "";
                    String suffix = "";
                    if (pow < 0) {
                        pow = -pow;
                        prefix = "1.0d / (";
                        suffix = ")";
                    }

                    switch (pow) {
                        case 1:
                            return prefix + mathematicaFullFormToJava(operands.get(0)) + suffix;
                        case 2:
                            return prefix + "sq(" + mathematicaFullFormToJava(operands.get(0)) + ')' + suffix;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
                return "Math.pow(" + mathematicaFullFormToJava(operands.get(0)) + ", " + mathematicaFullFormToJava(operands.get(1)) + ')';
            case "sqrt":
                assert operands.size() == 1 : fullForm;
                return "ROOT" + operands.get(0);
        }

        throw new IllegalArgumentException(fullForm);
    }*/

    protected void triangleTransform(double x, double y, double z, Vector2d dst) {
        triangleTransformDymaxion(x, y, z, dst);
    }

    protected static void inverseTriangleTransformNewton(double xpp, double ypp, Vector3d dst) {
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

        dst.x = z * xpoZ;
        dst.y = z * ypoZ;
        dst.z = z;
    }

    protected static void inverseTriangleTransformNewtonDerivative(TransformResourceCache cache, double xpp, double ypp, Matrix3x2 dst) {
        Vector3d vec = cache.inverseTriangleTransformNewton_temporaryVector;
        Matrix2x3 matrix = cache.inverseTriangleTransformNewton_temporaryMatrix;

        inverseTriangleTransformNewton(xpp, ypp, vec);
        triangleTransformDymaxionDerivative(vec.x, vec.y, vec.z, matrix);
        TMatrices.pseudoInvertFast(matrix, dst);
    }

    protected void inverseTriangleTransform(double x, double y, Vector3d dst) {
        inverseTriangleTransformNewton(x, y, dst);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);

        TransformResourceCache cache = TRANSFORM_RESOURCE_CACHE.get();

        Vector2d spherical = cache.spherical;
        Vector3d cartesian = cache.cartesian;
        Vector3d rotated = cache.rotated;
        Vector2d projected = cache.projected;

        TerraUtils.geo2Spherical(longitude, latitude, spherical);
        TerraUtils.spherical2Cartesian(spherical.x, spherical.y, cartesian);

        int face = findTriangle(cartesian);

        //apply rotation matrix (move triangle onto template triangle)
        TMatrices.multiplyFast(ROTATION_MATRICES[face], cartesian, rotated);
        this.triangleTransform(rotated.x, rotated.y, rotated.z, projected);

        //flip triangle to correct orientation
        final double projectedX = projected.x * FLIP_TRIANGLE_FACTOR[face];
        final double projectedY = projected.y * FLIP_TRIANGLE_FACTOR[face];

        double effectiveProjectedX = projectedX;
        double effectiveProjectedY = projectedY;
        //deal with special snowflakes (child faces 20, 21)
        if (((face == 15 && projectedX > projectedY * TerraUtils.ROOT3) || face == 14) && projectedX > 0) {
            effectiveProjectedX = 0.5 * projectedX - 0.5 * TerraUtils.ROOT3 * projectedY;
            effectiveProjectedY = 0.5 * TerraUtils.ROOT3 * projectedX + 0.5 * projectedY;
            face += 6; //shift 14->20 & 15->21
        }

        return new double[]{
                effectiveProjectedX + CENTER_MAP[face].x,
                effectiveProjectedY + CENTER_MAP[face].y,
        };
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        int face = findTriangleGrid(x, y);

        if (face == -1) {
            throw OutOfProjectionBoundsException.get();
        }

        x -= CENTER_MAP[face].x;
        y -= CENTER_MAP[face].y;

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
        x *= FLIP_TRIANGLE_FACTOR[face];
        y *= FLIP_TRIANGLE_FACTOR[face];

        TransformResourceCache cache = TRANSFORM_RESOURCE_CACHE.get();

        Vector2d geo = cache.geo;
        Vector2d spherical = cache.spherical;
        Vector3d cartesian = cache.cartesian;
        Vector3d rotated = cache.rotated;

        //invert triangle transform
        this.inverseTriangleTransform(x, y, rotated);

        //apply inverse rotation matrix (move triangle from template triangle to correct position on globe)
        TMatrices.multiplyFast(INVERSE_ROTATION_MATRICES[face], rotated, cartesian);

        //convert back to geo coordinates
        TerraUtils.cartesian2Spherical(cartesian.x, cartesian.y, cartesian.z, spherical);
        TerraUtils.spherical2Geo(spherical.x, spherical.y, geo);
        return new double[]{ geo.x, geo.y };
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

    private static final Cached<TransformResourceCache> TRANSFORM_RESOURCE_CACHE = Cached.threadLocal(TransformResourceCache::new);

    protected static class TransformResourceCache {
        public final Vector2d geo = new Vector2d();
        public final Vector2d spherical = new Vector2d();
        public final Vector3d cartesian = new Vector3d();
        public final Vector3d rotated = new Vector3d();
        public final Vector2d projected = new Vector2d();

        public final Matrix3x2 cartesianDerivative = Matrix3x2.createZero();
        public final Matrix3x2 rotatedDerivative = Matrix3x2.createZero();
        public final Matrix2x3 projectedDerivative = Matrix2x3.createZero();
        public final Matrix2 totalDerivative = new Matrix2();

        public final Vector3d inverseTriangleTransformNewton_temporaryVector = new Vector3d();
        public final Matrix2x3 inverseTriangleTransformNewton_temporaryMatrix = Matrix2x3.createZero();
    }

    public static final class OperationMethod extends AbstractOperationMethod.ForLegacyProjection {
        public OperationMethod() {
            super("Dymaxion");
        }

        @Override
        protected AbstractFromGeoMathTransform2D createBaseTransform(ParameterValueGroup parameters) throws InvalidParameterNameException, ParameterNotFoundException, InvalidParameterValueException {
            return new FromGeo<>(parameters, new ToGeo<>(parameters, TRANSFORM_RESOURCE_CACHE), TRANSFORM_RESOURCE_CACHE);
        }
    }

    protected static class FromGeo<CACHE extends TransformResourceCache> extends AbstractFromGeoMathTransform2D {
        private static final Matrix2 SPECIAL_FACTOR = new Matrix2(0.5d, -0.5d * ROOT3, 0.5d * ROOT3, 0.5d);

        protected final Cached<CACHE> cacheCache;

        public FromGeo(@NonNull ParameterValueGroup parameters, @NonNull ToGeo<CACHE> toGeo, @NonNull Cached<CACHE> cacheCache) {
            super(parameters, toGeo);

            this.cacheCache = cacheCache;
        }

        protected void triangleTransform(Vector3d rotated, Vector2d dst) {
            triangleTransformDymaxion(rotated, dst);
        }

        protected void triangleTransformDerivative(CACHE cache, Vector3d rotated, Matrix2x3 dst) {
            triangleTransformDymaxionDerivative(rotated, dst);
        }

        @Override
        protected void configureMatrices(ContextualParameters contextualParameters, MatrixSIS normalize, MatrixSIS denormalize) {
            //TerraUtils.geo2Spherical()
            normalize.convertAfter(0, DoubleDouble.createDegreesToRadians(), null);
            normalize.convertAfter(1, -1L, 90.0d); //90 - geo[1]
            normalize.convertAfter(1, DoubleDouble.createDegreesToRadians(), null);
        }

        @Override
        public Matrix2 transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
            CACHE cache = this.cacheCache.get();

            Vector3d cartesian = cache.cartesian;
            Vector3d rotated = cache.rotated;
            Vector2d projected = cache.projected;

            //there is no bounds checking here!

            final double origLon = srcPts[srcOff + 0];
            final double origLat = srcPts[srcOff + 1];

            TerraUtils.spherical2Cartesian(origLon, origLat, cartesian);
            final int origFace = findTriangle(cartesian);

            //apply rotation matrix (move triangle onto template triangle)
            TMatrices.multiplyFast(ROTATION_MATRICES[origFace], cartesian, rotated);
            this.triangleTransform(rotated, projected);

            //flip triangle to correct orientation
            final double origProjectedX = projected.x * FLIP_TRIANGLE_FACTOR[origFace];
            final double origProjectedY = projected.y * FLIP_TRIANGLE_FACTOR[origFace];

            if (dstPts != null) {
                double effectiveProjectedX = origProjectedX;
                double effectiveProjectedY = origProjectedY;
                int effectiveOffsetFace = origFace;

                //deal with special snowflakes (child faces 20, 21)
                if (((origFace == 15 && origProjectedX > ROOT3 * origProjectedY) || origFace == 14) && origProjectedX > 0) {
                    effectiveProjectedX = 0.5d * origProjectedX - 0.5d * ROOT3 * origProjectedY;
                    effectiveProjectedY = 0.5d * ROOT3 * origProjectedX + 0.5d * origProjectedY;
                    effectiveOffsetFace += 6; //shift 14->20 & 15->21
                }

                dstPts[dstOff + 0] = effectiveProjectedX + CENTER_MAP[effectiveOffsetFace].x;
                dstPts[dstOff + 1] = effectiveProjectedY + CENTER_MAP[effectiveOffsetFace].y;
            }
            if (!derivate) {
                return null;
            }

            Matrix3x2 cartesianDerivative = cache.cartesianDerivative;
            Matrix3x2 rotatedDerivative = cache.rotatedDerivative;
            Matrix2x3 projectedDerivative = cache.projectedDerivative;
            Matrix2 totalDerivative = cache.totalDerivative;

            TerraUtils.spherical2CartesianDerivative(origLon, origLat, cartesianDerivative);
            TMatrices.multiplyFast(ROTATION_MATRICES[origFace], cartesianDerivative, rotatedDerivative);
            this.triangleTransformDerivative(cache, rotated, projectedDerivative);
            TMatrices.multiplyFast(projectedDerivative, rotatedDerivative, totalDerivative);

            //flip triangle to correct orientation
            TMatrices.scaleFast(totalDerivative, FLIP_TRIANGLE_FACTOR[origFace], totalDerivative);

            //deal with special snowflakes (child faces 20, 21)
            if (((origFace == 15 && origProjectedX > ROOT3 * origProjectedY) || origFace == 14) && origProjectedX > 0) {
                return TMatrices.multiplyFast(SPECIAL_FACTOR, totalDerivative);
            } else {
                return totalDerivative.clone();
            }
        }
    }

    protected static class ToGeo<CACHE extends TransformResourceCache> extends AbstractToGeoMathTransform2D {
        protected final Cached<CACHE> cacheCache;

        public ToGeo(@NonNull ParameterValueGroup parameters, @NonNull Cached<CACHE> cacheCache) {
            super(parameters);

            this.cacheCache = cacheCache;
        }

        protected void inverseTriangleTransform(double x, double y, Vector3d dst) {
            inverseTriangleTransformNewton(x, y, dst);
        }

        protected void inverseTriangleTransformDerivative(CACHE cache, double x, double y, Matrix3x2 dst) {
            inverseTriangleTransformNewtonDerivative(cache, x, y, dst);
        }

        @Override
        public Matrix2 transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
            final double origX = srcPts[srcOff + 0];
            final double origY = srcPts[srcOff + 1];

            double x = origX;
            double y = origY;

            final int face = findTriangleGrid(x, y);
            if (face < 0) {
                throw OutOfProjectionBoundsException.get();
            }
            x -= CENTER_MAP[face].x;
            y -= CENTER_MAP[face].y;

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
            x *= FLIP_TRIANGLE_FACTOR[face];
            y *= FLIP_TRIANGLE_FACTOR[face];

            CACHE cache = this.cacheCache.get();

            Vector2d spherical = cache.spherical;
            Vector3d cartesian = cache.cartesian;
            Vector3d rotated = cache.rotated;

            //invert triangle transform
            this.inverseTriangleTransform(x, y, rotated);

            //apply inverse rotation matrix (move triangle from template triangle to correct position on globe)
            TMatrices.multiplyFast(INVERSE_ROTATION_MATRICES[face], rotated, cartesian);

            if (dstPts != null) {
                //convert back to geo coordinates
                TerraUtils.cartesian2Spherical(cartesian.x, cartesian.y, cartesian.z, spherical);

                //spherical -> geographic conversion is handled afterwards by the affine transform
                dstPts[dstOff + 0] = spherical.x;
                dstPts[dstOff + 1] = spherical.y;
            }
            if (!derivate) {
                return null;
            }

            Matrix3x2 rotatedDerivative = cache.rotatedDerivative;
            Matrix3x2 cartesianDerivative = cache.cartesianDerivative;
            Matrix2x3 sphericalDerivative = cache.projectedDerivative;
            Matrix2 totalDerivative = cache.totalDerivative;

            this.inverseTriangleTransformDerivative(cache, x, y, rotatedDerivative);
            TMatrices.multiplyFast(INVERSE_ROTATION_MATRICES[face], rotatedDerivative, cartesianDerivative);
            TerraUtils.cartesian2SphericalDerivative(cartesian.x, cartesian.y, cartesian.z, sphericalDerivative);
            TMatrices.multiplyFast(sphericalDerivative, cartesianDerivative, totalDerivative);

            //flip triangle to correct orientation
            TMatrices.scaleFast(totalDerivative, FLIP_TRIANGLE_FACTOR[face], totalDerivative);

            return totalDerivative.clone();
        }
    }
}