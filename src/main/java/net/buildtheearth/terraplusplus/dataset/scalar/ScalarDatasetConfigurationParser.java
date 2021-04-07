package net.buildtheearth.terraplusplus.dataset.scalar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.dataset.geojson.GeoJson;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.format.TileFormat;
import net.buildtheearth.terraplusplus.dataset.scalar.tile.mode.TileMode;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.TerraUtils;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.buildtheearth.terraplusplus.util.http.Http;
import net.buildtheearth.terraplusplus.util.jackson.IntListDeserializer;

import java.io.DataInput;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Helper class for parsing scalar datasets using the new format introduced in Terra++ 2.0.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class ScalarDatasetConfigurationParser {
    public CompletableFuture<IScalarDataset> loadAndMergeDatasetsFromManifests(@NonNull Stream<String[]> srcs) {
        return TerraUtils.mergeFuturesAsync(srcs.map(ScalarDatasetConfigurationParser::loadDatasetsFromManifest))
                .thenApply(list -> merge(list.stream().flatMap(Stream::of)));
    }

    public CompletableFuture<BoundedPriorityScalarDataset[]> loadDatasetsFromManifest(@NonNull String[] src) {
        return Http.getFirst(Http.suffixAll(src, "manifest.json"), Manifest::parse)
                .thenComposeAsync(manifest -> {
                    if (manifest == null) {
                        throw new IllegalStateException("unable to find manifest file at any of the given source URLs: " + Arrays.toString(Http.suffixAll(src, "manifest.json")));
                    }

                    for (int i = 0; i < manifest.datasets.length; i++) { //flatten all URLs
                        manifest.datasets[i] = Http.flatten(src, manifest.datasets[i]);
                    }

                    return TerraUtils.mergeFuturesAsync(Stream.of(manifest.datasets).map(datasetUrls -> loadDataset(manifest.priority, datasetUrls)))
                            .thenApply(list -> list.toArray(new BoundedPriorityScalarDataset[0]));
                });
    }

    public CompletableFuture<BoundedPriorityScalarDataset> loadDataset(double priority, @NonNull String... urls) {
        return Http.getFirst(Http.suffixAll(urls, "dataset.json"), Dataset::parse)
                .thenApplyAsync(dataset -> {
                    if (dataset == null) {
                        throw new IllegalStateException("unable to find dataset file at any of the given source URLs: " + Arrays.toString(Http.suffixAll(urls, "dataset.json")));
                    }

                    return dataset.toScalar(urls, priority);
                });
    }

    public IScalarDataset merge(@NonNull Stream<BoundedPriorityScalarDataset> datasets) {
        return new MultiScalarDataset(datasets.toArray(BoundedPriorityScalarDataset[]::new));
    }

    /**
     * Representation of a multi-dataset manifest JSON object.
     *
     * @author DaPorkchop_
     */
    @JsonDeserialize
    protected static final class Manifest {
        @SneakyThrows(IOException.class)
        public static Manifest parse(@NonNull ByteBuf data) {
            return JSON_MAPPER.readValue((DataInput) new ByteBufInputStream(data), Manifest.class);
        }

        public final String[][] datasets;
        public final double priority;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Manifest(
                @JsonProperty(value = "version", required = true) @NonNull Version version, //validation is done in Version constructor during deserialization
                @JsonProperty(value = "datasets", required = true) @NonNull String[][] datasets,
                @JsonProperty(value = "priority", required = true) double priority) {
            for (int i = 0; i < datasets.length; i++) {
                checkArg(datasets[i].length >= 1, "dataset must have at least one URL! @datasets[%d]", i);
            }
            this.datasets = datasets;
            this.priority = priority;
        }
    }

    /**
     * Representation of an individual dataset JSON object.
     *
     * @author DaPorkchop_
     */
    @JsonDeserialize
    protected static final class Dataset {
        @SneakyThrows(IOException.class)
        public static Dataset parse(@NonNull ByteBuf data) {
            return JSON_MAPPER.readValue((DataInput) new ByteBufInputStream(data), Dataset.class);
        }

        protected final int[] zoom;
        protected final Tiles tiles;
        protected final Bounds bounds;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Dataset(
                @JsonProperty(value = "version", required = true) @NonNull Version version, //validation is done in Version constructor during deserialization
                @JsonProperty(value = "zoom", required = true) @JsonDeserialize(using = IntListDeserializer.class) @NonNull int[] zoom,
                @JsonProperty(value = "tiles", required = true) @NonNull Tiles tiles,
                @JsonProperty(value = "bounds", required = true) @NonNull Bounds bounds) {
            checkArg(zoom.length >= 1, "at least one zoom level must be set!");

            this.zoom = zoom;
            this.tiles = tiles;
            this.bounds = bounds;
        }

        public BoundedPriorityScalarDataset toScalar(@NonNull String[] urls, double priority) {
            return new BoundedPriorityScalarDataset(
                    new MultiresScalarDataset(IntStream.of(this.zoom).mapToObj(zoom -> this.tiles.toScalar(urls, zoom)).toArray(IScalarDataset[]::new)),
                    this.bounds.build(), priority);
        }

        @JsonDeserialize
        protected static class Tiles {
            protected final GeographicProjection projection;
            protected final int resolution;
            protected final TileMode mode;
            protected final TileFormat format;

            @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
            public Tiles(
                    @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection,
                    @JsonProperty(value = "resolution", required = true) int resolution,
                    @JsonProperty(value = "mode", required = true) @NonNull TileMode mode,
                    @JsonProperty(value = "format", required = true) @NonNull TileFormat format) {
                this.projection = projection;
                this.resolution = resolution;
                this.mode = mode;
                this.format = format;
            }

            public IScalarDataset toScalar(@NonNull String[] urls, int zoom) {
                return new ZoomedTiledScalarDataset(urls, this.resolution, zoom, this.mode, this.format, this.projection);
            }
        }

        @JsonDeserialize
        protected static class Bounds {
            protected final GeographicProjection projection;
            protected final JsonNode geometryJson;

            @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
            public Bounds(
                    @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection,
                    @JsonProperty(value = "geometry", required = true) @NonNull JsonNode geometryJson) {
                this.projection = projection;
                this.geometryJson = geometryJson;
            }

            @SneakyThrows(OutOfProjectionBoundsException.class)
            public Bounds2d build() {
                return GeoJson.parseGeometry(this.geometryJson.toString())
                        .project(this.projection::toGeo)
                        .bounds();
            }
        }
    }

    /**
     * Dummy class which validates that the dataset version is set to 1.0.
     *
     * @author DaPorkchop_
     */
    @JsonDeserialize
    protected static final class Version {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Version(
                @JsonProperty(value = "major", required = true) int major,
                @JsonProperty(value = "minor", required = true) int minor) {
            checkArg(major == 1, "unsupported dataset version: %d.%d", major, minor);
        }
    }
}
