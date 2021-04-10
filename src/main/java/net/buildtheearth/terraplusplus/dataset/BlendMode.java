package net.buildtheearth.terraplusplus.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.util.IntToDoubleBiFunction;
import net.daporkchop.lib.math.grid.Grid2d;
import net.daporkchop.lib.math.interpolation.CubicInterpolation;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
@AllArgsConstructor
public enum BlendMode {
    LINEAR(0.0d, 2) {
        @Override
        public double get(double scaledX, double scaledZ, @NonNull IntToDoubleBiFunction sampler) {
            //get the corners surrounding this block
            int sampleX = (int) floor(scaledX);
            int sampleZ = (int) floor(scaledZ);

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
        @Override
        public double get(double scaledX, double scaledZ, @NonNull IntToDoubleBiFunction sampler) {
            //TODO: optimize this (eliminate allocation)
            return CubicInterpolation.instance().getInterpolated(scaledX, scaledZ, new Grid2d() {
                @Override
                public int startX() {
                    return Integer.MIN_VALUE;
                }

                @Override
                public int endX() {
                    return Integer.MAX_VALUE;
                }

                @Override
                public int startY() {
                    return Integer.MIN_VALUE;
                }

                @Override
                public int endY() {
                    return Integer.MAX_VALUE;
                }

                @Override
                public double getD(int x, int y) {
                    return sampler.apply(x, y);
                }

                @Override
                public int getI(int x, int y) {
                    return (int) sampler.apply(x, y);
                }

                @Override
                public void setD(int x, int y, double val) {
                }

                @Override
                public void setI(int x, int y, int val) {
                }
            });
        }
    };

    public final double offset;
    public final int size;

    public abstract double get(double scaledX, double scaledZ, @NonNull IntToDoubleBiFunction sampler);
}
