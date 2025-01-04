package net.buildtheearth.terraminusminus.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.buildtheearth.terraminusminus.util.IntToDoubleBiFunction;

import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor
public enum BlendMode {
    NEAR(0.0d, 0) {
        @Override
        public double get(double scaledX, double scaledZ, @NonNull IntToDoubleBiFunction sampler) {
            //very simple - just round down
            return sampler.apply(floorI(scaledX), floorI(scaledZ));
        }
    },
    LINEAR(0.0d, 2) {
        @Override
        public double get(double scaledX, double scaledZ, @NonNull IntToDoubleBiFunction sampler) {
            //get the corners surrounding this block
            int sampleX = floorI(scaledX);
            int sampleZ = floorI(scaledZ);

            double fx = scaledX - sampleX;
            double fz = scaledZ - sampleZ;

            double v00 = sampler.apply(sampleX, sampleZ);
            double v01 = sampler.apply(sampleX, sampleZ + 1);
            double v10 = sampler.apply(sampleX + 1, sampleZ);
            double v11 = sampler.apply(sampleX + 1, sampleZ + 1);

            return lerp(lerp(v00, v01, fz), lerp(v10, v11, fz), fx);
        }
    },
    @JsonAlias("SMOOTH") //old name
    CUBIC(-0.5d, 3) {
        //TODO: this is very broken and causes tons of artifacts

        /**
         * Lerping produces visible square patches.
         *
         * Fade-curve lerping doesn't work well on steep slopes.
         *
         * Standard splines require 16 control points.
         *
         * This requires only 9 control points to produce a smooth interpolation.
         *
         * @author K.jpg
         */
        double compute(double fx, double fy, double v00, double v01, double v02, double v10, double v11, double v12, double v20, double v21, double v22) {
            // Smooth fade curve. Using this directly in a lerp wouldn't work well for steep slopes.
            // But using it here with gradient ramps does a better job.
            double xFade = fx * fx * (3.0d - 2.0d * fx);
            double yFade = fy * fy * (3.0d - 2.0d * fy);

            // Centerpoints of each square. The interpolator meets these values exactly.
            double vAA = (v00 + v01 + v10 + v11) * 0.25d;
            double vAB = (v01 + v02 + v11 + v12) * 0.25d;
            double vBA = (v10 + v20 + v11 + v21) * 0.25d;
            double vBB = (v11 + v21 + v12 + v22) * 0.25d;

            // Slopes at each centerpoint.
            // We "should" divide by 2. But we divide x and y by 2 instead for the same result.
            double vAAx = ((v10 + v11) - (v00 + v01));
            double vAAy = ((v01 + v11) - (v00 + v10));
            double vABx = ((v11 + v12) - (v01 + v02));
            double vABy = ((v02 + v12) - (v01 + v11));
            double vBAx = ((v20 + v21) - (v10 + v11));
            double vBAy = ((v11 + v21) - (v10 + v20));
            double vBBx = ((v21 + v22) - (v11 + v12));
            double vBBy = ((v12 + v22) - (v11 + v21));

            // This is where we correct for the doubled slopes.
            // Note that it means we need x-0.5 instead of x-1.
            fx *= 0.5d;
            fy *= 0.5d;
            double ix = fx - 0.5d;
            double iy = fy - 0.5d;

            // extrapolate gradients and blend
            double blendXA = (1.0d - xFade) * (vAA + vAAx * fx + vAAy * fy) + xFade * (vBA + vBAx * ix + vBAy * fy);
            double blendXB = (1.0d - xFade) * (vAB + vABx * fx + vABy * iy) + xFade * (vBB + vBBx * ix + vBBy * iy);
            return (1.0d - yFade) * blendXA + yFade * blendXB;
        }

        @Override
        public double get(double scaledX, double scaledZ, @NonNull IntToDoubleBiFunction sampler) {
            double x = scaledX - 0.5d;
            double z = scaledZ - 0.5d;

            //get the corners surrounding this block
            int sampleX = floorI(x);
            int sampleZ = floorI(z);

            double fx = x - sampleX;
            double fz = z - sampleZ;

            double v00 = sampler.apply(sampleX, sampleZ);
            double v01 = sampler.apply(sampleX, sampleZ + 1);
            double v02 = sampler.apply(sampleX, sampleZ + 2);
            double v10 = sampler.apply(sampleX + 1, sampleZ);
            double v11 = sampler.apply(sampleX + 1, sampleZ + 1);
            double v12 = sampler.apply(sampleX + 1, sampleZ + 2);
            double v20 = sampler.apply(sampleX + 2, sampleZ);
            double v21 = sampler.apply(sampleX + 2, sampleZ + 1);
            double v22 = sampler.apply(sampleX + 2, sampleZ + 2);

            //Compute smooth 9-point interpolation on this block
            double result = this.compute(fx, fz, v00, v01, v02, v10, v11, v12, v20, v21, v22);

            if (result > 0.0d && v00 <= 0.0d && v10 <= 0.0d && v20 <= 0.0d && v21 <= 0.0d && v11 <= 0.0d && v01 <= 0.0d && v02 <= 0.0d && v12 <= 0.0d && v22 <= 0.0d) {
                return 0.0d; //anti ocean ridges
            }

            return result;
        }
    };

    public final double offset;
    public final int size;

    public abstract double get(double scaledX, double scaledZ, @NonNull IntToDoubleBiFunction sampler);
}
