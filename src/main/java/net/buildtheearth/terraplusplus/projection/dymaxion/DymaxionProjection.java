package net.buildtheearth.terraplusplus.projection.dymaxion;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.MathUtils;

/**
 * Implementation of the Dynmaxion projection.
 * Also known as Airocean or Fuller projection.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Dymaxion_map">Wikipedia's article on the Dynmaxion projection</a>
 */
@JsonDeserialize
public class DymaxionProjection implements GeographicProjection {

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
		{10.536199, 64.700000},
		{-5.245390, 2.300882},
		{58.157706, 10.447378},
		{122.300000, 39.100000},
		{-143.478490, 50.103201},
		{-67.132330, 23.717925},
		{36.521510, -50.103200},
		{112.867673, -23.717930},
		{174.754610, -2.300882},
		{-121.842290, -10.447350},
		{-57.700000, -39.100000},
		{-169.463800, -64.700000},
	};
    
	/**
	 * Indicates the vertices forming each face of the icosahedron.
	 * Each entry refers to the index of a vertex in {@link #VERTICES}
	 */
	protected static final int[][] ISO = {
		{2, 1, 6},
		{1, 0, 2},
		{0, 1, 5},
		{1, 5, 10},
		{1, 6, 10},
		{7, 2, 6},
		{2, 3, 7},
		{3, 0, 2},
		{0, 3, 4},
		{4, 0, 5}, //9, qubec
		{5, 4, 9},
		{9, 5, 10},
		{10, 9, 11},
		{11, 6, 10},
		{6, 7, 11},
		{8, 3, 7},
		{8, 3, 4},
		{8, 4, 9},
		{9, 8, 11},
		{7, 8, 11},
		{11, 6, 7}, //child of 14
		{3, 7, 8} //child of 15
	};
    
    protected static final double[][] CENTER_MAP = {
            {-3, 7},
            {-2, 5},
            {-1, 7},
            {2, 5},
            {4, 5},
            {-4, 1},
            {-3, -1},
            {-2, 1},
            {-1, -1},
            {0, 1},
            {1, -1},
            {2, 1},
            {3, -1},
            {4, 1},
            {5, -1}, //14, left side, right to be cut
            {-3, -5},
            {-1, -5},
            {1, -5},
            {2, -7},
            {-4, -7},
            {-5, -5}, //20, pseudo triangle, child of 14
            {-2, -7} //21 , pseudo triangle, child of 15
    };
    
	/**
	 * Indicates for each face if it needs to be flipped after projecting
	 */
	protected static final boolean[] FLIP_TRIANGLE = {
			true, false, true, false , false,
			true, false, true, false, true, false, true, false, true, false,
			true, true, true , false, false,
			true, false
	};
    
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
            CENTER_MAP[i][1] *= ARC * MathUtils.ROOT3 / 12;
        }

		// Will contain the list of vertices in Cartesian coordinates
		double[][] verticesCartesian = new double[VERTICES.length][3];

		// Convert the geographic vertices to spherical in radians
		for(int i=0; i < VERTICES.length; i++) {
			double[] vertexSpherical = MathUtils.geo2Spherical(VERTICES[i]);
			double[] vertex = MathUtils.spherical2Cartesian(vertexSpherical);
			verticesCartesian[i] = vertex;
			VERTICES[i] = vertexSpherical;
		}

		for(int i = 0; i < 22; i++) {

			// Vertices of the current face
			double[] vec1 = verticesCartesian[ISO[i][0]];
			double[] vec2 = verticesCartesian[ISO[i][1]];
			double[] vec3 = verticesCartesian[ISO[i][2]];
			
			// Find the centroid's projection onto the sphere
            double xsum = vec1[0] + vec2[0] + vec3[0];
            double ysum = vec1[1] + vec2[1] + vec3[1];
            double zsum = vec1[2] + vec2[2] + vec3[2];
            double mag = Math.sqrt(xsum * xsum + ysum * ysum + zsum * zsum);
			CENTROIDS[i] = new double[] {xsum / mag, ysum / mag, zsum / mag};

			double[] centroidSpherical = MathUtils.cartesian2Spherical(CENTROIDS[i]);
			double centroidLambda = centroidSpherical[0];
			double centroidPhi = centroidSpherical[1];

			double vertex[] = VERTICES[ISO[i][0]];
			double v[] = new double[] {vertex[0] - centroidLambda, vertex[1]};
			v = yRot(v, -centroidPhi);

			ROTATION_MATRICES[i] = MathUtils.produceZYZRotationMatrix(-centroidLambda, -centroidPhi, (Math.PI/2) - v[0]);
			INVERSE_ROTATION_MATRICES[i] = MathUtils.produceZYZRotationMatrix(v[0] - (Math.PI/2), centroidPhi, centroidLambda);

		}
    }

	/**
	 * Finds the face of the icosahedron on which to project a point.
	 * In practice, it works by finding the face with the closest centroid to the point.
	 * 
	 * @param vector - position vector as double array of length 3, using Cartesian coordinates
	 * @return an integer identifying the face on which to project the point
	 */
    protected int findTriangle(double[] vector) {

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

    protected static int findTriangleGrid(double x, double y) {

        //cast equilateral triangles to 45 degrees right triangles (side length of root2)
        double xp = x / ARC;
        double yp = y / (ARC * MathUtils.ROOT3);

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
        double[] c = MathUtils.spherical2Cartesian(spherical);

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

    protected double[] triangleTransform(double[] vec) {

        double S = Z / vec[2];

        double xp = S * vec[0];
        double yp = S * vec[1];

        double a = Math.atan((2 * yp / MathUtils.ROOT3 - EL6) / DVE); //ARC/2 terms cancel
        double b = Math.atan((xp - yp / MathUtils.ROOT3 - EL6) / DVE);
        double c = Math.atan((-xp - yp / MathUtils.ROOT3 - EL6) / DVE);

        return new double[]{ 0.5 * (b - c), (2 * a - b - c) / (2 * MathUtils.ROOT3) };
    }

    protected double[] inverseTriangleTransformNewton(double xpp, double ypp) {

        //a & b are linearly related to c, so using the tan of sum formula we know: tan(c+off) = (tanc + tanoff)/(1-tanc*tanoff)
        double tanaoff = Math.tan(MathUtils.ROOT3 * ypp + xpp); // a = c + root3*y'' + x''
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
        double yp = MathUtils.ROOT3 * (DVE * tana + EL6) / 2;
        double xp = DVE * tanb + yp / MathUtils.ROOT3 + EL6;

        //x = z*xp/Z, y = z*yp/Z, x^2 + y^2 + z^2 = 1
        double xpoZ = xp / Z;
        double ypoZ = yp / Z;

        double z = 1 / Math.sqrt(1 + xpoZ * xpoZ + ypoZ * ypoZ);

        return new double[]{ z * xpoZ, z * ypoZ, z };
    }

    protected double[] inverseTriangleTransform(double x, double y) {
        return this.inverseTriangleTransformNewton(x, y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) {

        double[] vector = MathUtils.spherical2Cartesian(MathUtils.geo2Spherical(new double[] {longitude, latitude}));

        int face = findTriangle(vector);

        //apply rotation matrix (move triangle onto template triangle)
        double[] pvec = MathUtils.matVecProdD(ROTATION_MATRICES[face], vector);
        double[] projectedVec = this.triangleTransform(pvec);

        //flip triangle to correct orientation
        if (FLIP_TRIANGLE[face]) {
            projectedVec[0] = -projectedVec[0];
            projectedVec[1] = -projectedVec[1];
        }

        vector[0] = projectedVec[0];
        //deal with special snowflakes (child faces 20, 21)
        if (((face == 15 && vector[0] > projectedVec[1] * MathUtils.ROOT3) || face == 14) && vector[0] > 0) {
            projectedVec[0] = 0.5 * vector[0] - 0.5 * MathUtils.ROOT3 * projectedVec[1];
            projectedVec[1] = 0.5 * MathUtils.ROOT3 * vector[0] + 0.5 * projectedVec[1];
            face += 6; //shift 14->20 & 15->21
        }

        projectedVec[0] += CENTER_MAP[face][0];
        projectedVec[1] += CENTER_MAP[face][1];

        return projectedVec;
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        int face = findTriangleGrid(x, y);

        if (face == -1) throw OutOfProjectionBoundsException.get();

        x -= CENTER_MAP[face][0];
        y -= CENTER_MAP[face][1];

        //deal with bounds of special snowflakes
        switch (face) {
            case 14:
                if (x > 0) throw OutOfProjectionBoundsException.get();
                break;
            case 20:
                if (-y * MathUtils.ROOT3 > x) throw OutOfProjectionBoundsException.get();
                break;
            case 15:
                if (x > 0 && x > y * MathUtils.ROOT3) throw OutOfProjectionBoundsException.get();
                break;
            case 21:
                if (x < 0 || -y * MathUtils.ROOT3 > x) throw OutOfProjectionBoundsException.get();
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

        double[] vec = {x, y, z};
        //apply inverse rotation matrix (move triangle from template triangle to correct position on globe)
        double[] vecp = MathUtils.matVecProdD(INVERSE_ROTATION_MATRICES[face], vec);
        
        //convert back to geo coordinates
        return MathUtils.spherical2Geo(MathUtils.cartesian2Spherical(vecp));
    }

    @Override
    public double[] bounds() {
        return new double[]{ -3 * ARC, -0.75 * ARC * MathUtils.ROOT3, 2.5 * ARC, 0.75 * ARC * MathUtils.ROOT3 };
    }

    @Override
    public boolean upright() {
        return false;
    }

    @Override
    public double metersPerUnit() {
        return Math.sqrt(510100000000000.0 / (20 * MathUtils.ROOT3 * ARC * ARC / 4));
    }

    @Override
    public String toString() {
        return "Dymaxion";
    }
}