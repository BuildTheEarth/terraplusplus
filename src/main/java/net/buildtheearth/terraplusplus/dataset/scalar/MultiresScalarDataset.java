package net.buildtheearth.terraplusplus.dataset.scalar;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Implementation of {@link IScalarDataset} which combines multiple datasets that all cover the same area at varying resolutions.
 * <p>
 * Point queries will be resolved using the highest-resolution dataset.
 * <p>
 * Area queries will be resolved using the dataset whose resolution is closest to that of the requested area (rounded up).
 *
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
        //calculate the degrees/sample of the query bounds
        double dps = bounds.avgDegreesPerSample(sizeX, sizeZ);

        //find the dataset whose resolution is closest (rounding up)
        Map.Entry<Double, IScalarDataset> entry = this.datasetsByDegreesPerSample.floorEntry(dps);
        //fall back to max resolution dataset if none match (the query BB is higher-res than the best dataset)
        IScalarDataset dataset = entry != null ? entry.getValue() : this.maxResDataset;
        return dataset.getAsync(bounds, sizeX, sizeZ);
    }

    @Override
    public double[] degreesPerSample() {
        return this.maxResDataset.degreesPerSample();
    }
}
