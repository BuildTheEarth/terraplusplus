package net.buildtheearth.terraplusplus.dataset.scalar;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
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

    /**
     * A {@link BoundedPriorityScalarDataset} which may be serialized as an arbitrary Jackson-serializable value.
     *
     * @author DaPorkchop_
     */
    @JsonSerialize
    @Getter(onMethod_ = { @JsonValue })
    public static class Serializable extends BoundedPriorityScalarDataset {
        protected final Object toSerializeAs;

        public Serializable(@NonNull IScalarDataset delegate, @NonNull Bounds2d bounds, double priority, Object toSerializeAs) {
            super(delegate, bounds, priority);

            this.toSerializeAs = toSerializeAs;
        }
    }
}
