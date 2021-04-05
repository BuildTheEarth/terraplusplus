package net.buildtheearth.terraplusplus.dataset.scalar;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public class BoundedPriorityScalarDataset implements Bounds2d, IScalarDataset, Comparable<BoundedPriorityScalarDataset> {
    @NonNull
    protected final IScalarDataset delegate;
    @NonNull
    protected final Bounds2d bounds;
    protected final double priority;

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
    public double sampleCountIn(@NonNull CornerBoundingBox2d bounds) throws OutOfProjectionBoundsException {
        if (!this.bounds.intersects(bounds.axisAlign())) {
            //entire bounding box is out of bounds, don't bother checking
            return 0.0d;
        }
        return this.delegate.sampleCountIn(bounds);
    }

    @Override
    public int compareTo(BoundedPriorityScalarDataset o) {
        return -Double.compare(this.priority, o.priority);
    }

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
