package io.github.terra121.dataset;

import com.google.gson.GsonBuilder;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.BVH;
import io.github.terra121.util.bvh.Bounds2d;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.daporkchop.lib.binary.oio.reader.UTF8FileReader;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * Implementation of {@link ScalarDataset} which can sample from multiple {@link ScalarDataset}s at different resolutions and combine the results.
 *
 * @author DaPorkchop_
 */
public class MultiresDataset implements ScalarDataset {
    protected final GeographicProjection projection;

    protected final BVH<WrappedDataset> bvh;

    public MultiresDataset(@NonNull GeographicProjection projection, @NonNull URL configSource, @NonNull BiFunction<Integer, String[], ScalarDataset> mapper) throws IOException {
        this.projection = projection;

        List<TempWrappedDataset> datasetsIn;
        try (Reader reader = new UTF8FileReader(configSource.openStream())) {
            datasetsIn = Arrays.asList(new GsonBuilder().setLenient().create().fromJson(reader, TempWrappedDataset[].class));
        }

        this.bvh = new BVH<>(datasetsIn.stream()
                .map(t -> t.toWrapped(mapper))
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Double> getAsync(double lon, double lat) throws OutOfProjectionBoundsException {
        CompletableFuture<Double>[] futures = uncheckedCast(this.bvh.getAllIntersecting(Bounds2d.of(lon, lon, lat, lat)).stream()
                .sorted() //sort so that the array is ordered by priority
                .map(w -> {
                    try {
                        return w.dataset.getAsync(lon, lat);
                    } catch (OutOfProjectionBoundsException e) {
                        return CompletableFuture.completedFuture(Double.NaN);
                    }
                })
                .toArray(CompletableFuture[]::new));

        if (futures.length == 0) { //no matching datasets!
            return CompletableFuture.completedFuture(Double.NaN);
        } else if (futures.length == 1) { //only one dataset matches, there's no reason to do any merging
            return futures[0];
        }

        return CompletableFuture.allOf(futures)
                .thenApplyAsync(unused -> {
                    for (CompletableFuture<Double> future : futures) { //iterate through requested datasets in order
                        Double v = future.join();
                        if (!Double.isNaN(v)) { //find first non-NaN value and return it
                            return v;
                        }
                    }

                    return Double.NaN;
                });
    }

    @Override
    public CompletableFuture<double[]> getAsync(@NonNull CornerBoundingBox2d bounds, int sizeX, int sizeZ) throws OutOfProjectionBoundsException {
        if (notNegative(sizeX, "sizeX") == 0 | notNegative(sizeZ, "sizeZ") == 0) { //no input points -> no output points, ez
            return CompletableFuture.completedFuture(new double[0]);
        }

        CompletableFuture<double[]>[] futures = uncheckedCast(this.bvh.getAllIntersecting(bounds).stream()
                .sorted() //sort so that the array is ordered by priority
                .map(w -> {
                    try {
                        return w.dataset.getAsync(bounds, sizeX, sizeZ);
                    } catch (OutOfProjectionBoundsException e) {
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .toArray(CompletableFuture[]::new));

        if (futures.length == 0) { //no matching datasets!
            return CompletableFuture.completedFuture(null);
        } else if (futures.length == 1) { //only one dataset matches, there's no reason to do any merging
            return futures[0];
        }

        return CompletableFuture.allOf(futures)
                .thenApplyAsync(unused -> {
                    int len = sizeX * sizeZ;
                    int remaining = len;
                    double[] out = new double[len];
                    Arrays.fill(out, Double.NaN);

                    TOP:
                    for (CompletableFuture<double[]> future : futures) { //iterate through requested datasets in order
                        double[] data = future.join();
                        checkState(data.length == len);
                        for (int i = 0; i < len; i++) {
                            if (Double.isNaN(out[i])) { //if value in output array is NaN, consider replacing it
                                double v = data[i];
                                if (!Double.isNaN(v)) { //if the value in the input array is not NaN, it's now the output
                                    out[i] = v;
                                    if (--remaining == 0) { //if no samples are left to process, break out early
                                        break TOP;
                                    }
                                }
                            }
                        }
                    }

                    return out;
                });
    }

    /**
     * Temporary class used while deserializing config.
     *
     * @author DaPorkchop_
     */
    private static final class TempWrappedDataset {
        private String[] urls;
        private double minX = Double.NaN;
        private double minZ = Double.NaN;
        private double maxX = Double.NaN;
        private double maxZ = Double.NaN;
        @Getter
        private int zoom = -1;
        private double priority = 0.0d;

        public WrappedDataset toWrapped(@NonNull BiFunction<Integer, String[], ScalarDataset> mapper) {
            checkState(this.urls != null && this.urls.length > 0, "urls must be set!");
            checkState(!Double.isNaN(this.minX), "minX must be set!");
            checkState(!Double.isNaN(this.minZ), "minZ must be set!");
            checkState(!Double.isNaN(this.maxX), "maxX must be set!");
            checkState(!Double.isNaN(this.maxZ), "maxZ must be set!");
            checkState(this.zoom >= 0, "zoom must be set!");

            double minX = min(this.minX, this.maxX);
            double minZ = min(this.minZ, this.maxZ);
            double maxX = max(this.minX, this.maxX);
            double maxZ = max(this.minZ, this.maxZ);

            return new WrappedDataset(mapper.apply(this.zoom, this.urls), minX, maxX, minZ, maxZ, this.zoom, this.priority);
        }
    }

    /**
     * Wrapper around a dataset with a bounding box.
     *
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class WrappedDataset implements Bounds2d, Comparable<WrappedDataset> {
        @NonNull
        protected final ScalarDataset dataset;

        protected final double minX;
        protected final double maxX;
        protected final double minZ;
        protected final double maxZ;

        protected final int zoom;

        protected final double priority;

        @Override
        public int compareTo(WrappedDataset o) {
            if (this.zoom != o.zoom) {
                return -Integer.compare(this.zoom, o.zoom);
            }
            return -Double.compare(this.priority, o.priority);
        }
    }
}
