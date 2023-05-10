package net.buildtheearth.terraplusplus.projection.dymaxion;

import LZMA.LzmaInputStream;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.projection.sis.AbstractOperationMethod;
import net.buildtheearth.terraplusplus.projection.sis.transform.AbstractFromGeoMathTransform2D;
import net.buildtheearth.terraplusplus.util.math.matrix.Matrix2x3;
import net.buildtheearth.terraplusplus.util.math.matrix.Matrix3x2;
import net.buildtheearth.terraplusplus.util.math.matrix.TMatrices;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;
import net.daporkchop.lib.common.util.PArrays;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.io.InputStream;

import static net.buildtheearth.terraplusplus.util.TerraUtils.*;

/**
 * Implementation of the Dynmaxion like conformal projection.
 * Slightly modifies the Dynmaxion projection to make it (almost) conformal.
 *
 * @see DymaxionProjection
 */
@JsonDeserialize
public class ConformalDynmaxionProjection extends DymaxionProjection {
    protected static final double VECTOR_SCALE_FACTOR = 1.0d / 1.1473979730192934d;
    protected static final int SIDE_LENGTH = 256;

    protected static final Cached<InvertableVectorField> INVERSE_CACHE = Cached.global((IOSupplier<InvertableVectorField>) () -> {
        Vector2d[][] vecs = PArrays.filledBy(SIDE_LENGTH + 1, Vector2d[][]::new, i -> new Vector2d[SIDE_LENGTH + 1 - i]);

        ByteBuf buf;
        try (InputStream in = new LzmaInputStream(ConformalDynmaxionProjection.class.getResourceAsStream("conformal.lzma"))) {
            buf = Unpooled.wrappedBuffer(StreamUtil.toByteArray(in));
        }

        for (int v = 0; v < SIDE_LENGTH + 1; v++) {
            for (int u = 0; u < SIDE_LENGTH + 1 - v; u++) {
                vecs[u][v] = new Vector2d(buf.readDouble() * VECTOR_SCALE_FACTOR, buf.readDouble() * VECTOR_SCALE_FACTOR);
            }
        }

        return new InvertableVectorField(vecs);
    }, ReferenceStrength.SOFT);

    protected final InvertableVectorField inverse = INVERSE_CACHE.get();

    @Override
    protected void triangleTransform(double x, double y, double z, Vector2d dst) {
        triangleTransformDymaxion(x, y, z, dst);

        //use another interpolated vector to have a really good guess before using Newton's method
        //Note: foward was removed for now, will need to be added back if this improvement is ever re-implemented
        //c = forward.getInterpolatedVector(c[0], c[1]);
        //c = inverse.applyNewtonsMethod(x, y, c[0]/ARC + 0.5, c[1]/ARC + ROOT3/6, 1);

        //just use newtons method: slower
        this.inverse.applyNewtonsMethod(dst.x, dst.y, 5, dst, null);

        dst.x -= 0.5d;
        dst.y -= ROOT3 / 6.0d;

        dst.x *= ARC;
        dst.y *= ARC;
    }

    @Override
    protected void inverseTriangleTransform(double x, double y, Vector3d dst) {
        x /= ARC;
        y /= ARC;

        x += 0.5;
        y += ROOT3 / 6;

        InvertableVectorField.Result result = InvertableVectorField.RESULT_CACHE.get();
        this.inverse.getInterpolatedVector(x, y, result);
        super.inverseTriangleTransform(result.f, result.g, dst);
    }

    @Override
    public String toString() {
        return "Conformal Dymaxion";
    }

    @RequiredArgsConstructor
    protected static final class InvertableVectorField {
        private static final Cached<Result> RESULT_CACHE = Cached.threadLocal(Result::new);

        private final Vector2d[][] vecs;

        public void getInterpolatedVector(double x, double y, Result dst) {
            //scale up triangle to be triangleSize across
            x *= SIDE_LENGTH;
            y *= SIDE_LENGTH;

            //convert to triangle units
            double v = 2 / ROOT3 * y;
            double u = x - v * 0.5;

            int u1 = (int) u;
            int v1 = (int) v;

            if (u1 < 0) {
                u1 = 0;
            } else if (u1 >= SIDE_LENGTH) {
                u1 = SIDE_LENGTH - 1;
            }

            if (v1 < 0) {
                v1 = 0;
            } else if (v1 >= SIDE_LENGTH - u1) {
                v1 = SIDE_LENGTH - u1 - 1;
            }

            double valx1;
            double valy1;
            double valx2;
            double valy2;
            double valx3;
            double valy3;
            double y3;
            double x3;

            double flip;

            if (y < -ROOT3 * (x - u1 - v1 - 1) || v1 == SIDE_LENGTH - u1 - 1) {
                Vector2d vec1 = this.vecs[u1][v1];
                Vector2d vec2 = this.vecs[u1][v1 + 1];
                Vector2d vec3 = this.vecs[u1 + 1][v1];

                valx1 = vec1.x;
                valy1 = vec1.y;
                valx2 = vec2.x;
                valy2 = vec2.y;
                valx3 = vec3.x;
                valy3 = vec3.y;

                flip = 1;

                y3 = 0.5 * ROOT3 * v1;
                x3 = (u1 + 1) + 0.5 * v1;
            } else {
                Vector2d vec1 = this.vecs[u1][v1 + 1];
                Vector2d vec2 = this.vecs[u1 + 1][v1];
                Vector2d vec3 = this.vecs[u1 + 1][v1 + 1];

                valx1 = vec1.x;
                valy1 = vec1.y;
                valx2 = vec2.x;
                valy2 = vec2.y;
                valx3 = vec3.x;
                valy3 = vec3.y;

                flip = -1;
                y = -y;

                y3 = -(0.5 * ROOT3 * (v1 + 1));
                x3 = (u1 + 1) + 0.5 * (v1 + 1);
            }

            //TODO: not sure if weights are right (but weirdly mirrors stuff so there may be simplifcation yet)
            double w1 = -(y - y3) / ROOT3 - (x - x3);
            double w2 = 2 / ROOT3 * (y - y3);
            double w3 = 1 - w1 - w2;

            dst.f = valx1 * w1 + valx2 * w2 + valx3 * w3;
            dst.g = valy1 * w1 + valy2 * w2 + valy3 * w3;
            dst.dfdx = (valx3 - valx1) * SIDE_LENGTH;
            dst.dfdy = SIDE_LENGTH / ROOT3 * flip * (2 * valx2 - valx1 - valx3);
            dst.dgdx = (valy3 - valy1) * SIDE_LENGTH;
            dst.dgdy = SIDE_LENGTH / ROOT3 * flip * (2 * valy2 - valy1 - valy3);
        }

        public void applyNewtonsMethod(double expectedf, double expectedg, int iter, Vector2d dst, Matrix2 derivativeDst) {
            //porkman's notes from trying to reverse-engineer this:
            //  - we're trying to solve for (xest, yest) such that
            //        getInterpolatedVector((xest, yest)) - (expectedf, expectedg) = (0, 0)

            double xest = expectedf / ARC + 0.5d;
            double yest = expectedg / ARC + (ROOT3 / 6.0d);

            Result result = RESULT_CACHE.get();

            for (int i = 0; i < iter; i++) {
                this.getInterpolatedVector(xest, yest, result);

                double f = result.f - expectedf;
                double g = result.g - expectedg;

                double determinant = 1 / (result.dfdx * result.dgdy - result.dfdy * result.dgdx);

                xest -= determinant * (result.dgdy * f - result.dfdy * g);
                yest -= determinant * (-result.dgdx * f + result.dfdx * g);
            }

            dst.x = xest;
            dst.y = yest;

            if (derivativeDst != null) {
                derivativeDst.m00 = result.dfdx;
                derivativeDst.m01 = result.dfdy;
                derivativeDst.m10 = result.dgdx;
                derivativeDst.m11 = result.dgdy;
                TMatrices.invertFast(derivativeDst, derivativeDst);
            }
        }

        public static final class Result {
            public double f;
            public double g;
            public double dfdx;
            public double dfdy;
            public double dgdx;
            public double dgdy;
        }
    }

    protected static final Cached<TransformResourceCache> TRANSFORM_RESOURCE_CACHE = Cached.threadLocal(TransformResourceCache::new);

    protected static class TransformResourceCache extends DymaxionProjection.TransformResourceCache {
        public final Vector2d conformal_superTriangleTransform = new Vector2d();
        public final Matrix2 conformal_newtonDerivative = new Matrix2();
    }

    public static final class OperationMethod extends AbstractOperationMethod.ForLegacyProjection {
        public OperationMethod() {
            super("Conformal Dymaxion");
        }

        @Override
        protected AbstractFromGeoMathTransform2D createBaseTransform(ParameterValueGroup parameters) throws InvalidParameterNameException, ParameterNotFoundException, InvalidParameterValueException {
            return new FromGeo<>(parameters, new ToGeo<>(parameters, TRANSFORM_RESOURCE_CACHE), TRANSFORM_RESOURCE_CACHE);
        }
    }

    protected static class FromGeo<CACHE extends TransformResourceCache> extends DymaxionProjection.FromGeo<CACHE> {
        private final InvertableVectorField field = INVERSE_CACHE.get();

        public FromGeo(@NonNull ParameterValueGroup parameters, @NonNull ToGeo<CACHE> toGeo, @NonNull Cached<CACHE> cacheCache) {
            super(parameters, toGeo, cacheCache);
        }

        @Override
        protected void triangleTransform(Vector3d rotated, Vector2d dst) {
            super.triangleTransform(rotated, dst);

            //use another interpolated vector to have a really good guess before using Newton's method
            //Note: foward was removed for now, will need to be added back if this improvement is ever re-implemented
            //c = forward.getInterpolatedVector(c[0], c[1]);
            //c = inverse.applyNewtonsMethod(x, y, c[0]/ARC + 0.5, c[1]/ARC + ROOT3/6, 1);

            //just use newtons method: slower
            this.field.applyNewtonsMethod(dst.x, dst.y, 5, dst, null);

            dst.x -= 0.5d;
            dst.y -= ROOT3 / 6.0d;

            dst.x *= ARC;
            dst.y *= ARC;
        }

        @Override
        protected void triangleTransformDerivative(CACHE cache, Vector3d rotated, Matrix2x3 dst) {
            Vector2d superTransform = cache.conformal_superTriangleTransform;
            Matrix2 newtonDerivative = cache.conformal_newtonDerivative;

            super.triangleTransformDerivative(cache, rotated, dst);
            super.triangleTransform(rotated, superTransform);

            this.field.applyNewtonsMethod(superTransform.x, superTransform.y, 5, superTransform, newtonDerivative);
            TMatrices.scaleFast(newtonDerivative, ARC, newtonDerivative);

            TMatrices.multiplyFast(newtonDerivative, dst, dst);
        }
    }

    protected static class ToGeo<CACHE extends TransformResourceCache> extends DymaxionProjection.ToGeo<CACHE> {
        private final InvertableVectorField field = INVERSE_CACHE.get();

        public ToGeo(@NonNull ParameterValueGroup parameters, @NonNull Cached<CACHE> cacheCache) {
            super(parameters, cacheCache);
        }

        @Override
        protected void inverseTriangleTransform(double x, double y, Vector3d dst) {
            x /= ARC;
            y /= ARC;

            x += 0.5d;
            y += ROOT3 / 6.0d;

            InvertableVectorField.Result result = InvertableVectorField.RESULT_CACHE.get();
            this.field.getInterpolatedVector(x, y, result);
            super.inverseTriangleTransform(result.f, result.g, dst);
        }

        @Override
        protected void inverseTriangleTransformDerivative(CACHE cache, double x, double y, Matrix3x2 dst) {
            Matrix2 newtonDerivative = cache.conformal_newtonDerivative;

            x /= ARC;
            y /= ARC;

            x += 0.5d;
            y += ROOT3 / 6.0d;

            InvertableVectorField.Result result = InvertableVectorField.RESULT_CACHE.get();
            this.field.getInterpolatedVector(x, y, result);

            newtonDerivative.m00 = result.dfdx;
            newtonDerivative.m01 = result.dfdy;
            newtonDerivative.m10 = result.dgdx;
            newtonDerivative.m11 = result.dgdy;
            TMatrices.scaleFast(newtonDerivative, 1.0d / ARC, newtonDerivative);

            super.inverseTriangleTransformDerivative(cache, result.f, result.g, dst);

            TMatrices.multiplyFast(dst, newtonDerivative, dst);
        }
    }
}
