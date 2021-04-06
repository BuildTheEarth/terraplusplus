package net.buildtheearth.terraplusplus.dataset.scalar;

import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.BVH;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Implementation of {@link IScalarDataset} which can sample from multiple {@link IScalarDataset}s and combine the results.
 *
 * @author DaPorkchop_
 */
public class MultiScalarDataset implements IScalarDataset {
    protected final BVH<BoundedPriorityScalarDataset> bvh;

    public MultiScalarDataset(@NonNull BoundedPriorityScalarDataset... sources) {
        this.bvh = BVH.of(sources);
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        BoundedPriorityScalarDataset[] datasets = this.bvh.getAllIntersecting(Bounds2d.of(lon, lon, lat, lat)).toArray(new BoundedPriorityScalarDataset[0]);
        if (datasets.length == 0) { //no matching datasets!
            return CompletableFuture.completedFuture(Double.NaN);
        } else if (datasets.length == 1) { //only one dataset matches
            return datasets[0].getAsync(lon, lat);
        }
        Arrays.sort(datasets); //ensure datasets are in priority order

        class State implements BiConsumer<Double, Throwable> {
            final CompletableFuture<Double> future = new CompletableFuture<>();
            int i = -1;

            @Override
            public void accept(Double v, Throwable cause) {
                if (cause != null) {
                    this.future.completeExceptionally(cause);
                } else if (!Double.isNaN(v)) { //if the value in the input array is accepted, use it as the output
                    this.future.complete(v);
                } else { //sample the next dataset
                    this.advance();
                }
            }

            private void advance() {
                if (++this.i < datasets.length) {
                    try {
                        datasets[this.i].getAsync(lon, lat).whenComplete(this);
                    } catch (OutOfProjectionBoundsException e) {
                        this.future.completeExceptionally(e);
                    }
                } else { //no datasets remain, complete the future successfully with whatever value we currently have
                    this.future.complete(Double.NaN);
                }
            }
        }

        State state = new State();
        state.advance();
        return state.future;
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        if (notNegative(sizeX, "sizeX") == 0 | notNegative(sizeZ, "sizeZ") == 0) { //no input points -> no output points, ez
            return CompletableFuture.completedFuture(new double[0]);
        }

        BoundedPriorityScalarDataset[] datasets = this.bvh.getAllIntersecting(bounds).toArray(new BoundedPriorityScalarDataset[0]);
        if (datasets.length == 0) { //no matching datasets!
            return CompletableFuture.completedFuture(null);
        } else if (datasets.length == 1) { //only one dataset matches
            return datasets[0].getAsync(bounds, sizeX, sizeZ);
        }
        Arrays.sort(datasets); //ensure datasets are in priority order

        class State implements BiConsumer<double[], Throwable> {
            final CompletableFuture<double[]> future = new CompletableFuture<>();
            double[] out;
            int remaining = sizeX * sizeZ;
            int i = -1;

            @Override
            public void accept(double[] data, Throwable cause) {
                if (cause != null) {
                    this.future.completeExceptionally(cause);
                } else if (data != null) { //if the array is null, it's as if it were an array of NaNs - nothing would be set, we simply skip it
                    double[] out = this.out;
                    if (out == null) { //ensure the destination array is set
                        Arrays.fill(this.out = out = new double[sizeX * sizeZ], Double.NaN);
                    }

                    BoundedPriorityScalarDataset dataset = datasets[this.i];

                    for (int i = 0; i < sizeX * sizeZ; i++) {
                        if (Double.isNaN(out[i])) { //if value in output array is NaN, consider replacing it
                            double v = data[i];
                            if (!Double.isNaN(v)) { //if the value in the input array is accepted, use it as the output
                                out[i] = v;
                                if (--this.remaining == 0) { //if no samples are left to process, we're done!
                                    this.future.complete(out);
                                    return;
                                }
                            }
                        }
                    }
                }
                this.advance();
            }

            private void advance() {
                if (++this.i < datasets.length) {
                    try {
                        datasets[this.i].getAsync(bounds, sizeX, sizeZ).whenComplete(this);
                    } catch (OutOfProjectionBoundsException e) {
                        this.future.completeExceptionally(e);
                    }
                } else { //no datasets remain, complete the future successfully with whatever value we currently have
                    this.future.complete(this.out);
                }
            }
        }

        State state = new State();
        state.advance();
        return state.future;
    }
}
