package net.buildtheearth.terraminusminus.dataset.builtin;

import static net.daporkchop.lib.common.util.PValidation.notNegative;

import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.dataset.IScalarDataset;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.util.CornerBoundingBox2d;

/**
 * Abstract implementation of {@link IScalarDataset} for the builtin datasets.
 *
 * @author DaPorkchop_
 */
public abstract class AbstractBuiltinDataset implements IScalarDataset {
    protected final double scaleX;
    protected final double scaleY;

    public AbstractBuiltinDataset(int samplesX, int samplesY) {
        this.scaleX = samplesX / 360.0d;
        this.scaleY = samplesY / 180.0d;
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(lon, lat);
        return CompletableFuture.completedFuture(this.get((lon + 180.0d) * this.scaleX, (90.0d - lat) * this.scaleY));
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        if (notNegative(sizeX, "sizeX") == 0 | notNegative(sizeZ, "sizeZ") == 0) { //no input points -> no output points, ez
            return CompletableFuture.completedFuture(new double[0]);
        }

        return CompletableFuture.supplyAsync(() -> {
            double stepX = 1.0d / sizeX;
            double stepZ = 1.0d / sizeZ;

            double scaleX = this.scaleX;
            double scaleY = this.scaleY;

            double[] point = new double[2];
            double[] out = new double[sizeX * sizeZ];

            double fx = 0.0d;
            for (int i = 0, x = 0; x < sizeX; x++, fx += stepX) {
                double fz = 0.0d;
                for (int z = 0; z < sizeZ; z++, fz += stepZ) {
                    //compute coordinates of point
                    point = bounds.point(point, fx, fz);

                    //sample value at point
                    out[i++] = this.get((point[0] + 180.0d) * scaleX, (90.0d - point[1]) * scaleY);
                }
            }

            return out;
        });
    }

    protected abstract double get(double x, double y);
}
