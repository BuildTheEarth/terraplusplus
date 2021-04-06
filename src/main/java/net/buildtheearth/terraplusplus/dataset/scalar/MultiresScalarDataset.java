package net.buildtheearth.terraplusplus.dataset.scalar;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public class MultiresScalarDataset implements IScalarDataset {
    protected final NavigableMap<Double, IScalarDataset> datasetsByDegreesPerSample;
    protected final IScalarDataset maxResDataset;

    public MultiresScalarDataset(@NonNull IScalarDataset... datasets) {
        checkArg(datasets.length >= 1, "at least one dataset must be given!");

        this.datasetsByDegreesPerSample = new TreeMap<>();
        for (IScalarDataset dataset : datasets) {
            double[] degreesPerSample = dataset.degreesPerSample();
            this.datasetsByDegreesPerSample.put(min(degreesPerSample[0], degreesPerSample[1]), dataset);
        }
        this.maxResDataset = this.datasetsByDegreesPerSample.firstEntry().getValue();
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        return this.maxResDataset.getAsync(lon, lat);
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        Bounds2d aabb = bounds.axisAlign();
        double degreesPerSample = min((aabb.maxX() - aabb.minX()) / sizeX, (aabb.maxZ() - aabb.minZ()) / sizeZ);

        Map.Entry<Double, IScalarDataset> entry = this.datasetsByDegreesPerSample.floorEntry(degreesPerSample);
        return (entry != null ? entry.getValue() : this.maxResDataset).getAsync(bounds, sizeX, sizeZ);
    }

    @Override
    public double[] degreesPerSample() {
        return this.maxResDataset.degreesPerSample();
    }
}
