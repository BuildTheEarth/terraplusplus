package net.buildtheearth.terraplusplus.projection.dymaxion;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.sis.AbstractOperationMethod;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractFromGeoMathTransform2D;
import net.buildtheearth.terraplusplus.util.TerraUtils;
import net.buildtheearth.terraplusplus.util.math.matrix.TMatrices;
import net.daporkchop.lib.common.pool.array.ArrayAllocator;
import net.daporkchop.lib.common.reference.cache.Cached;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.operation.matrix.MatrixSIS;
import org.apache.sis.referencing.operation.transform.ContextualParameters;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.TransformException;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;

/**
 * Implementation of the BTE modified Dynmaxion projection.
 *
 * @see DymaxionProjection
 * @see ConformalDynmaxionProjection
 */
@JsonDeserialize
public class BTEDymaxionProjection extends ConformalDynmaxionProjection {

    protected static final double THETA = Math.toRadians(-150);
    protected static final double SIN_THETA = Math.sin(THETA);
    protected static final double COS_THETA = Math.cos(THETA);
    protected static final double BERING_X = -0.3420420960118339;//-0.3282152608138795;
    protected static final double BERING_Y = -0.322211064085279;//-0.3281491467713469;
    protected static final double ARCTIC_Y = -0.2;//-0.3281491467713469;
    protected static final double ARCTIC_M = (ARCTIC_Y - TerraUtils.ROOT3 * ARC / 4) / (BERING_X - -0.5 * ARC);
    protected static final double ARCTIC_B = ARCTIC_Y - ARCTIC_M * BERING_X;
    protected static final double ALEUTIAN_Y = -0.5000446805492526;//-0.5127463765943157;
    protected static final double ALEUTIAN_XL = -0.5149231279757507;//-0.4957832938238718;
    protected static final double ALEUTIAN_XR = -0.45;
    protected static final double ALEUTIAN_M = (BERING_Y - ALEUTIAN_Y) / (BERING_X - ALEUTIAN_XR);
    protected static final double ALEUTIAN_B = BERING_Y - ALEUTIAN_M * BERING_X;

    protected static final Cached<double[]> TMP_LENGTH2_ARRAY_CACHE = Cached.threadLocal(() -> new double[2]);

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] c = super.fromGeo(longitude, latitude);
        double x = c[0];
        double y = c[1];

        boolean easia = isEurasianPart(x, y);

        y -= 0.75 * ARC * TerraUtils.ROOT3;

        if (easia) {
            x += ARC;

            double t = x;
            x = COS_THETA * x - SIN_THETA * y;
            y = SIN_THETA * t + COS_THETA * y;

        } else {
            x -= ARC;
        }

        c[0] = y;
        c[1] = -x;
        return c;
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        boolean easia;
        if (y < 0) {
            easia = x > 0;
        } else if (y > ARC / 2) {
            easia = x > -TerraUtils.ROOT3 * ARC / 2;
        } else {
            easia = y * -TerraUtils.ROOT3 < x;
        }

        double t = x;
        x = -y;
        y = t;

        if (easia) {
            t = x;
            x = COS_THETA * x + SIN_THETA * y;
            y = COS_THETA * y - SIN_THETA * t;
            x -= ARC;

        } else {
            x += ARC;
        }

        y += 0.75 * ARC * TerraUtils.ROOT3;

        //check to make sure still in right part
        if (easia != isEurasianPart(x, y)) {
            throw OutOfProjectionBoundsException.get();
        }

        return super.toGeo(x, y);
    }

    private static boolean isEurasianPart(double x, double y) {

        //catch vast majority of cases in not near boundary
        if (x > 0) {
            return false;
        }
        if (x < -0.5 * ARC) {
            return true;
        }

        if (y > TerraUtils.ROOT3 * ARC / 4) //above arctic ocean
        {
            return x < 0;
        }

        if (y < ALEUTIAN_Y) //below bering sea
        {
            return y < (ALEUTIAN_Y + ALEUTIAN_XL) - x;
        }

        if (y > BERING_Y) { //boundary across arctic ocean

            if (y < ARCTIC_Y) {
                return x < BERING_X; //in strait
            }

            return y < ARCTIC_M * x + ARCTIC_B; //above strait
        }

        return y > ALEUTIAN_M * x + ALEUTIAN_B;
    }

    @Override
    public double[] bounds() {
        return new double[]{ -1.5 * ARC * TerraUtils.ROOT3, -1.5 * ARC, 3 * ARC, TerraUtils.ROOT3 * ARC }; //TODO: 3*ARC is prly to high
    }

    @Override
    public String toString() {
        return "BuildTheEarth Conformal Dymaxion";
    }

    public static final class OperationMethod extends AbstractOperationMethod.ForLegacyProjection {
        public OperationMethod() {
            super("BuildTheEarth Conformal Dymaxion");
        }

        @Override
        protected AbstractFromGeoMathTransform2D createBaseTransform(ParameterValueGroup parameters) throws InvalidParameterNameException, ParameterNotFoundException, InvalidParameterValueException {
            return new FromGeo<>(parameters, new ToGeo<>(parameters, ConformalDynmaxionProjection.TRANSFORM_RESOURCE_CACHE), ConformalDynmaxionProjection.TRANSFORM_RESOURCE_CACHE);
        }
    }

    protected static class FromGeo<CACHE extends ConformalDynmaxionProjection.TransformResourceCache> extends ConformalDynmaxionProjection.FromGeo<CACHE> {
        private static final Matrix2 EURASIA_ROTATE_MATRIX = new Matrix2(COS_THETA, -SIN_THETA, SIN_THETA, COS_THETA);

        public FromGeo(@NonNull ParameterValueGroup parameters, @NonNull ToGeo<CACHE> toGeo, @NonNull Cached<CACHE> cacheCache) {
            super(parameters, toGeo, cacheCache);
        }

        @Override
        protected void configureMatrices(ContextualParameters contextualParameters, MatrixSIS normalize, MatrixSIS denormalize) {
            super.configureMatrices(contextualParameters, normalize, denormalize);

            //c[0] = y;
            //c[1] = -x;
            denormalize.setMatrix(Matrices.createDimensionSelect(2, new int[]{ 1, 0 }).multiply(denormalize));
            denormalize.convertAfter(1, -1L, null);
        }

        @Override
        public Matrix2 transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
            if (derivate && dstPts == null) { //the derivative was requested, we need to get the projected coordinates as well
                dstPts = TMP_LENGTH2_ARRAY_CACHE.get();
                dstOff = 0;
            }

            Matrix2 derivative = super.transform(srcPts, srcOff, dstPts, dstOff, derivate);

            if (dstPts != null) {
                double x = dstPts[dstOff + 0];
                double y = dstPts[dstOff + 1];

                boolean eurasia = isEurasianPart(x, y);

                y -= 0.75d * ARC * TerraUtils.ROOT3;

                if (eurasia) {
                    double x0 = x + ARC;
                    double y0 = y;
                    x = COS_THETA * x0 - SIN_THETA * y0;
                    y = SIN_THETA * x0 + COS_THETA * y0;

                    if (derivative != null) {
                        TMatrices.multiplyFast(EURASIA_ROTATE_MATRIX, derivative.clone(), derivative);
                    }
                } else {
                    x -= ARC;

                    //all the offsets are by a constant factor, so the derivative isn't affected
                }

                dstPts[dstOff + 0] = x;
                dstPts[dstOff + 1] = y;
            }

            return derivative;
        }
    }

    protected static class ToGeo<CACHE extends ConformalDynmaxionProjection.TransformResourceCache> extends ConformalDynmaxionProjection.ToGeo<CACHE> {
        private static final Matrix2 EURASIA_ROTATE_MATRIX = new Matrix2(COS_THETA, SIN_THETA, -SIN_THETA, COS_THETA);

        public ToGeo(@NonNull ParameterValueGroup parameters, @NonNull Cached<CACHE> cacheCache) {
            super(parameters, cacheCache);
        }

        @Override
        public Matrix2 transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) throws TransformException {
            double x = srcPts[srcOff + 0];
            double y = srcPts[srcOff + 1];

            boolean easia;
            if (y < 0) {
                easia = x > 0.0d;
            } else if (y > ARC / 2.0d) {
                easia = x > -TerraUtils.ROOT3 * ARC / 2.0d;
            } else {
                easia = y * -TerraUtils.ROOT3 < x;
            }

            double t = x;
            x = -y;
            y = t;

            Matrix2 preRotateMatrix = null;

            if (easia) {
                double x0 = x;
                double y0 = y;
                x = SIN_THETA * y0 + COS_THETA * x0 - ARC;
                y = COS_THETA * y0 - SIN_THETA * x0;

                preRotateMatrix = EURASIA_ROTATE_MATRIX;
            } else {
                x += ARC;
            }

            y += 0.75d * ARC * TerraUtils.ROOT3;

            //check to make sure still in right part
            if (easia != isEurasianPart(x, y)) {
                throw OutOfProjectionBoundsException.get();
            }

            srcPts = TMP_LENGTH2_ARRAY_CACHE.get();
            srcOff = 0;

            srcPts[srcOff + 0] = x;
            srcPts[srcOff + 1] = y;

            Matrix2 derivative = super.transform(srcPts, srcOff, dstPts, dstOff, derivate);

            if (preRotateMatrix != null && derivative != null) {
                TMatrices.multiplyFast(derivative.clone(), EURASIA_ROTATE_MATRIX, derivative); //TODO: operand order?
            }

            return derivative;
        }
    }
}