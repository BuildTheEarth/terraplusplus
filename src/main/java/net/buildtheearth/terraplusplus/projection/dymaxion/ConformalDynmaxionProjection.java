package net.buildtheearth.terraplusplus.projection.dymaxion;

import LZMA.LzmaInputStream;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.buildtheearth.terraplusplus.util.MathUtils;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.common.function.io.IOSupplier;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;
import net.daporkchop.lib.common.util.PArrays;

import java.io.InputStream;

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
        double[][] vx = PArrays.filledBy(SIDE_LENGTH + 1, double[][]::new, i -> new double[SIDE_LENGTH + 1 - i]);
        double[][] vy = PArrays.filledBy(SIDE_LENGTH + 1, double[][]::new, i -> new double[SIDE_LENGTH + 1 - i]);

        ByteBuf buf;
        try (InputStream in = new LzmaInputStream(ConformalDynmaxionProjection.class.getResourceAsStream("conformal.lzma"))) {
            buf = Unpooled.wrappedBuffer(StreamUtil.toByteArray(in));
        }

        for (int v = 0; v < SIDE_LENGTH + 1; v++) {
            for (int u = 0; u < SIDE_LENGTH + 1 - v; u++) {
                vx[u][v] = buf.readDouble() * VECTOR_SCALE_FACTOR;
                vy[u][v] = buf.readDouble() * VECTOR_SCALE_FACTOR;
            }
        }

        return new InvertableVectorField(vx, vy);
    }, ReferenceStrength.SOFT);

    protected final InvertableVectorField inverse = INVERSE_CACHE.get();

    @Override
    protected double[] triangleTransform(double[] vec) {
        double[] c = super.triangleTransform(vec);

        double x = c[0];
        double y = c[1];

        c[0] /= ARC;
        c[1] /= ARC;

        c[0] += 0.5;
        c[1] += MathUtils.ROOT3 / 6;

        //use another interpolated vector to have a really good guess before using Newton's method
        //Note: foward was removed for now, will need to be added back if this improvement is ever re-implemented
        //c = forward.getInterpolatedVector(c[0], c[1]);
        //c = inverse.applyNewtonsMethod(x, y, c[0]/ARC + 0.5, c[1]/ARC + ROOT3/6, 1);

        //just use newtons method: slower
        c = this.inverse.applyNewtonsMethod(x, y, c[0], c[1], 5);//c[0]/ARC + 0.5, c[1]/ARC + ROOT3/6

        c[0] -= 0.5;
        c[1] -= MathUtils.ROOT3 / 6;

        c[0] *= ARC;
        c[1] *= ARC;

        return c;
    }

    @Override
    protected double[] inverseTriangleTransform(double x, double y) {

        x /= ARC;
        y /= ARC;

        x += 0.5;
        y += MathUtils.ROOT3 / 6;

        double[] c = this.inverse.getInterpolatedVector(x, y);
        return super.inverseTriangleTransform(c[0], c[1]);
    }

    @Override
    public double metersPerUnit() {
        return (40075017.0d / (2.0d * Math.PI)) / VECTOR_SCALE_FACTOR;
    }

    @Override
    public String toString() {
        return "Conformal Dymaxion";
    }

    private static class InvertableVectorField {
        private final double[][] vx;
        private final double[][] vy;

        public InvertableVectorField(double[][] vx, double[][] vy) {
            this.vx = vx;
            this.vy = vy;
        }

        public double[] getInterpolatedVector(double x, double y) {
            //scale up triangle to be triangleSize across
            x *= SIDE_LENGTH;
            y *= SIDE_LENGTH;

            //convert to triangle units
            double v = 2 * y / MathUtils.ROOT3;
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

            double flip = 1;

            if (y < -MathUtils.ROOT3 * (x - u1 - v1 - 1) || v1 == SIDE_LENGTH - u1 - 1) {
                valx1 = this.vx[u1][v1];
                valy1 = this.vy[u1][v1];
                valx2 = this.vx[u1][v1 + 1];
                valy2 = this.vy[u1][v1 + 1];
                valx3 = this.vx[u1 + 1][v1];
                valy3 = this.vy[u1 + 1][v1];

                y3 = 0.5 * MathUtils.ROOT3 * v1;
                x3 = (u1 + 1) + 0.5 * v1;
            } else {
                valx1 = this.vx[u1][v1 + 1];
                valy1 = this.vy[u1][v1 + 1];
                valx2 = this.vx[u1 + 1][v1];
                valy2 = this.vy[u1 + 1][v1];
                valx3 = this.vx[u1 + 1][v1 + 1];
                valy3 = this.vy[u1 + 1][v1 + 1];

                flip = -1;
                y = -y;

                y3 = -(0.5 * MathUtils.ROOT3 * (v1 + 1));
                x3 = (u1 + 1) + 0.5 * (v1 + 1);
            }

            //TODO: not sure if weights are right (but weirdly mirrors stuff so there may be simplifcation yet)
            double w1 = -(y - y3) / MathUtils.ROOT3 - (x - x3);
            double w2 = 2 * (y - y3) / MathUtils.ROOT3;
            double w3 = 1 - w1 - w2;

            return new double[]{ valx1 * w1 + valx2 * w2 + valx3 * w3, valy1 * w1 + valy2 * w2 + valy3 * w3,
                    (valx3 - valx1) * SIDE_LENGTH, SIDE_LENGTH * flip * (2 * valx2 - valx1 - valx3) / MathUtils.ROOT3,
                    (valy3 - valy1) * SIDE_LENGTH, SIDE_LENGTH * flip * (2 * valy2 - valy1 - valy3) / MathUtils.ROOT3 };
        }

        public double[] applyNewtonsMethod(double expectedf, double expectedg, double xest, double yest, int iter) {
            for (int i = 0; i < iter; i++) {
                double[] c = this.getInterpolatedVector(xest, yest);

                double f = c[0] - expectedf;
                double g = c[1] - expectedg;
                double dfdx = c[2];
                double dfdy = c[3];
                double dgdx = c[4];
                double dgdy = c[5];

                double determinant = 1 / (dfdx * dgdy - dfdy * dgdx);

                xest -= determinant * (dgdy * f - dfdy * g);
                yest -= determinant * (-dgdx * f + dfdx * g);
            }

            return new double[]{ xest, yest };
        }
    }
}