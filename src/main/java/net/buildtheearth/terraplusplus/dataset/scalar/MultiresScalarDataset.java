package net.buildtheearth.terraplusplus.dataset.scalar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.config.condition.DoubleCondition;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.IntRange;
import net.buildtheearth.terraplusplus.util.bvh.BVH;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.buildtheearth.terraplusplus.util.http.Disk;
import net.daporkchop.lib.common.function.io.IOFunction;
import net.daporkchop.lib.common.function.throwing.EFunction;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Implementation of {@link IScalarDataset} which can sample from multiple {@link IScalarDataset}s at different resolutions and combine the results.
 *
 * @author DaPorkchop_
 */
public class MultiresScalarDataset implements IScalarDataset {
    protected final BVH<WrappedDataset> bvh;

    @SneakyThrows(IOException.class)
    public MultiresScalarDataset(@NonNull String name, boolean useDefault) {
        List<URL> configSources = new ArrayList<>();
        if (useDefault) { //add default configuration
            configSources.add(MultiresScalarDataset.class.getResource(name + ".json5"));
        }

        try (Stream<Path> stream = Files.list(Files.createDirectories(Disk.configFile(name)))) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().matches(".*\\.json5?$"))
                    .map(Path::toUri).map((EFunction<URI, URL>) URI::toURL)
                    .forEach(configSources::add);
        }

        this.bvh = BVH.of(configSources.stream()
                .map((IOFunction<URL, WrappedDataset[]>) url -> TerraConstants.JSON_MAPPER.readValue(url, WrappedDataset[].class))
                .flatMap(Arrays::stream)
                .toArray(WrappedDataset[]::new));
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        WrappedDataset[] datasets = this.bvh.getAllIntersecting(Bounds2d.of(lon, lon, lat, lat)).toArray(new WrappedDataset[0]);
        if (datasets.length == 0) { //no matching datasets!
            return CompletableFuture.completedFuture(Double.NaN);
        } else if (datasets.length == 1) { //only one dataset matches
            if (datasets[0].condition == null) { //if it doesn't have a condition, there's no reason to do any merging
                return datasets[0].dataset.getAsync(lon, lat);
            }
        }
        Arrays.sort(datasets); //ensure datasets are in priority order

        class State implements BiConsumer<Double, Throwable> {
            final CompletableFuture<Double> future = new CompletableFuture<>();
            int i = -1;

            @Override
            public void accept(Double v, Throwable cause) {
                if (cause != null) {
                    this.future.completeExceptionally(cause);
                } else if (!Double.isNaN(v) && datasets[this.i].test(v)) { //if the value in the input array is accepted, use it as the output
                    this.future.complete(v);
                } else { //sample the next dataset
                    this.advance();
                }
            }

            private void advance() {
                if (++this.i < datasets.length) {
                    try {
                        datasets[this.i].dataset.getAsync(lon, lat).whenComplete(this);
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

        WrappedDataset[] datasets = this.bvh.getAllIntersecting(bounds).toArray(new WrappedDataset[0]);
        if (datasets.length == 0) { //no matching datasets!
            return CompletableFuture.completedFuture(null);
        } else if (datasets.length == 1) { //only one dataset matches
            if (datasets[0].condition == null) { //if it doesn't have a condition, there's no reason to do any merging
                return datasets[0].dataset.getAsync(bounds, sizeX, sizeZ);
            }
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

                    WrappedDataset dataset = datasets[this.i];

                    for (int i = 0; i < sizeX * sizeZ; i++) {
                        if (Double.isNaN(out[i])) { //if value in output array is NaN, consider replacing it
                            double v = data[i];
                            if (!Double.isNaN(v) && dataset.test(v)) { //if the value in the input array is accepted, use it as the output
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
                        datasets[this.i].dataset.getAsync(bounds, sizeX, sizeZ).whenComplete(this);
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

    /**
     * Wrapper around a dataset with a bounding box.
     *
     * @author DaPorkchop_
     */
    @JsonDeserialize
    @JsonSerialize
    @Getter
    public static class WrappedDataset implements Bounds2d, Comparable<WrappedDataset>, DoubleCondition {
        @Getter(onMethod_ = { @JsonGetter })
        protected final IScalarDataset dataset;
        @Getter(onMethod_ = { @JsonGetter })
        protected final DoubleCondition condition;
        @Getter(onMethod_ = { @JsonGetter })
        protected final IntRange zooms; //TODO: use this

        protected final double minX;
        protected final double maxX;
        protected final double minZ;
        protected final double maxZ;

        @Getter(onMethod_ = { @JsonGetter })
        protected final double priority;

        @JsonCreator
        public WrappedDataset(
                @JsonProperty(value = "dataset", required = true) @NonNull IScalarDataset dataset,
                @JsonProperty(value = "bounds", required = true) @NonNull Bounds2d bounds,
                @JsonProperty(value = "zooms", required = true) @NonNull IntRange zooms,
                @JsonProperty(value = "priority", defaultValue = "0.0") double priority,
                @JsonProperty("condition") DoubleCondition condition) {
            this.dataset = dataset;
            this.condition = condition;
            this.zooms = zooms;
            this.priority = priority;

            this.minX = bounds.minX();
            this.maxX = bounds.maxX();
            this.minZ = bounds.minZ();
            this.maxZ = bounds.maxZ();
        }

        @Override
        public int compareTo(WrappedDataset o) {
            return -Double.compare(this.priority, o.priority);
        }

        @Override
        public boolean test(double value) {
            return this.condition == null || this.condition.test(value);
        }

        @JsonGetter("bounds")
        public Bounds2d bounds() {
            return Bounds2d.of(this.minX, this.maxX, this.minZ, this.maxZ);
        }
    }
}
