package net.buildtheearth.terraplusplus.dataset.scalar;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.TerraUtils;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.util.concurrent.CompletableFuture;
import java.util.stream.DoubleStream;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class BoundedPriorityScalarDataset implements Bounds2d, IScalarDataset, Comparable<BoundedPriorityScalarDataset> {
    @NonNull
    protected final IScalarDataset delegate;
    @NonNull
    protected final Bounds2d bounds;
    @NonNull
    protected final double[] priorities;

    // IScalarDataset

    @Override
    public CompletableFuture<Double> getAsync(@NonNull double[] point) throws OutOfProjectionBoundsException {
        return this.delegate.getAsync(point);
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        return this.delegate.getAsync(lon, lat);
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        return this.delegate.getAsync(bounds, sizeX, sizeZ);
    }

    @Override
    public double[] degreesPerSample() {
        return this.delegate.degreesPerSample();
    }

    // Comparable<BoundedPriorityScalarDataset>

    @Override
    public int compareTo(BoundedPriorityScalarDataset o) {
        int d = -TerraUtils.compareDoubleArrays(this.priorities, o.priorities);
        if (d == 0) { //priorities are equal, compare by resolution
            double dps0 = DoubleStream.of(this.degreesPerSample()).min().getAsDouble();
            double dps1 = DoubleStream.of(o.degreesPerSample()).min().getAsDouble();
            d = Double.compare(dps0, dps1);
        }
        return d;
    }

    // Bounds2d

    @Override
    public double minX() {
        return this.bounds.minX();
    }

    @Override
    public double maxX() {
        return this.bounds.maxX();
    }

    @Override
    public double minZ() {
        return this.bounds.minZ();
    }

    @Override
    public double maxZ() {
        return this.bounds.maxZ();
    }
}
