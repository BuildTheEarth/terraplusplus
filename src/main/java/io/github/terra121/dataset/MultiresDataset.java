package io.github.terra121.dataset;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.terra121.dataset.impl.Heights;
import io.github.terra121.projection.EquirectangularProjection;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.MapsProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CornerBoundingBox2d;
import io.github.terra121.util.bvh.BVH;
import io.github.terra121.util.bvh.Bounds2d;
import io.github.terra121.util.http.Disk;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.binary.oio.StreamUtil;
import net.daporkchop.lib.binary.oio.reader.UTF8FileReader;
import net.daporkchop.lib.common.function.io.IOFunction;
import net.daporkchop.lib.common.function.throwing.EFunction;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * Implementation of {@link ScalarDataset} which can sample from multiple {@link ScalarDataset}s at different resolutions and combine the results.
 *
 * @author DaPorkchop_
 */
public class MultiresDataset implements ScalarDataset {
    public static URL[] configSources(@NonNull String name) throws IOException {
        Path configDir = Files.createDirectories(Disk.configFile(name));

        Path defaultConfig = configDir.resolve("default.json5");
        if (!Files.isRegularFile(defaultConfig)) { //config file doesn't exist, create default one
            Path oldDefaultConfig = configDir.resolveSibling(name + "_config.json");
            if (Files.isRegularFile(oldDefaultConfig)) { //old config file exists, move it to directory
                Files.move(oldDefaultConfig, defaultConfig);
            } else {
                try (InputStream in = MultiresDataset.class.getResourceAsStream("/default_config/" + name + ".json5")) {
                    Files.write(defaultConfig, StreamUtil.toByteArray(in));
                }
            }
        }

        try (Stream<Path> stream = Files.list(configDir)) {
            return stream.map(Path::toUri).map((EFunction<URI, URL>) URI::toURL).toArray(URL[]::new);
        }
    }

    protected final GeographicProjection projection;

    protected final BVH<WrappedDataset> bvh;

    public MultiresDataset(@NonNull GeographicProjection projection, @NonNull URL[] configSources, @NonNull BiFunction<Integer, String[], ScalarDataset> mapper) throws IOException {
        this.projection = projection;

        List<TempWrappedDataset> datasetsIn = Arrays.stream(configSources)
                .map((IOFunction<URL, TempWrappedDataset[]>) url -> {
                    try (Reader reader = new UTF8FileReader(url.openStream())) {
                        //TODO: use TerraConstants.GSON once fast-osm is merged
                        return new GsonBuilder().setLenient().create().fromJson(reader, TempWrappedDataset[].class);
                    }
                })
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        this.bvh = new BVH<>(datasetsIn.stream()
                .flatMap(t -> t.toWrapped(mapper))
                .collect(Collectors.toList()));
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
     * Temporary class used while deserializing config.
     *
     * @author DaPorkchop_
     */
    private static final class TempWrappedDataset {
        private String[] urls;
        private Bounds[] bounds;
        @Getter
        private int zoom = -1;
        private double priority = 0.0d;

        private JsonObject condition;

        public Stream<WrappedDataset> toWrapped(@NonNull BiFunction<Integer, String[], ScalarDataset> mapper) {
            checkState(this.urls != null && this.urls.length > 0, "urls must be set!");
            checkState(this.bounds != null && this.bounds.length > 0, "bounds must be set!");
            for (Bounds bounds : this.bounds) {
                checkState(!Double.isNaN(bounds.minX), "minX must be set!");
                checkState(!Double.isNaN(bounds.minZ), "minZ must be set!");
                checkState(!Double.isNaN(bounds.maxX), "maxX must be set!");
                checkState(!Double.isNaN(bounds.maxZ), "maxZ must be set!");
            }
            checkState(this.zoom >= 0, "zoom must be set!");

            DoublePredicate condition = Condition.parse(this.condition);
            ScalarDataset dataset = mapper.apply(this.zoom, this.urls);
            return Stream.of(this.bounds)
                    .map(bounds -> {
                        double minX = min(bounds.minX, bounds.maxX);
                        double minZ = min(bounds.minZ, bounds.maxZ);
                        double maxX = max(bounds.minX, bounds.maxX);
                        double maxZ = max(bounds.minZ, bounds.maxZ);
                        return new WrappedDataset(dataset, condition, minX, maxX, minZ, maxZ, this.zoom, this.priority);
                    });
        }

        private static final class Bounds {
            private double minX = Double.NaN;
            private double minZ = Double.NaN;
            private double maxX = Double.NaN;
            private double maxZ = Double.NaN;
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
    public static class WrappedDataset implements Bounds2d, Comparable<WrappedDataset>, DoublePredicate {
        @NonNull
        @Getter(AccessLevel.NONE)
        protected final ScalarDataset dataset;
        @Getter(AccessLevel.NONE)
        protected final DoublePredicate condition;

        protected final double minX;
        protected final double maxX;
        protected final double minZ;
        protected final double maxZ;

        @Getter(AccessLevel.NONE)
        protected final int zoom;

        @Getter(AccessLevel.NONE)
        protected final double priority;

        @Override
        public int compareTo(WrappedDataset o) {
            return -Double.compare(this.priority, o.priority);
        }

        @Override
        public boolean test(double value) {
            return this.condition == null || this.condition.test(value);
        }
    }

    /**
     * Helper class for parsing usage conditions.
     *
     * @author DaPorkchop_
     */
    @UtilityClass
    private static class Condition {
        /**
         * Parses a condition object.
         *
         * @param in the raw condition read from the config
         * @return the parsed condition
         */
        public DoublePredicate parse(JsonObject in) {
            if (in == null) {
                return null;
            }

            checkArg(in.size() == 1, "condition has more than one operator: %s", in);
            Map.Entry<String, JsonElement> entry = in.entrySet().iterator().next();
            JsonElement element = entry.getValue();
            checkArg(!element.isJsonNull(), "operator has null value: %s", in);
            switch (entry.getKey()) {
                case "and": {
                    DoublePredicate[] delegates = parseMulti(in, element, "and");
                    return v -> {
                        for (DoublePredicate delegate : delegates) {
                            if (!delegate.test(v)) {
                                return false;
                            }
                        }
                        return true;
                    };
                }
                case "or": {
                    DoublePredicate[] delegates = parseMulti(in, element, "or");
                    return v -> {
                        for (DoublePredicate delegate : delegates) {
                            if (delegate.test(v)) {
                                return true;
                            }
                        }
                        return false;
                    };
                }
                case "not":
                    checkArg(element.isJsonObject(), "\"not\" requires an object: %s", in);
                    return parse(element.getAsJsonObject()).negate();
                case "lessThan": {
                    checkArg(element.isJsonPrimitive(), "\"lessThan\" requires a primitive: %s", in);
                    double threshold = element.getAsDouble();
                    return v -> v < threshold;
                }
                case "greaterThan": {
                    checkArg(element.isJsonPrimitive(), "\"greaterThan\" requires a primitive: %s", in);
                    double threshold = element.getAsDouble();
                    return v -> v > threshold;
                }
                default:
                    throw new IllegalArgumentException("unknown operator: \"" + entry.getValue() + '"');
            }
        }

        private DoublePredicate[] parseMulti(JsonObject root, JsonElement element, String modeName) {
            checkArg(element.isJsonArray(), "\"%s\" requires an array: %s", modeName, root);
            checkArg(element.getAsJsonArray().size() > 0, "\"%s\" requires at least one condition: %s", modeName, root);
            return StreamSupport.stream(element.getAsJsonArray().spliterator(), false)
                    .peek(child -> checkArg(child.isJsonObject(), "child condition not a json object: %s", child))
                    .map(JsonElement::getAsJsonObject)
                    .map(Condition::parse)
                    .toArray(DoublePredicate[]::new);
        }
    }
}
