package net.buildtheearth.terraplusplus.generator.biome;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.generator.ChunkBiomesBuilder;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.BVH;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.buildtheearth.terraplusplus.util.http.Disk;
import net.daporkchop.lib.common.function.io.IOFunction;
import net.daporkchop.lib.common.function.throwing.EFunction;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author DaPorkchop_
 */
public class UserOverrideBiomeFilter implements IEarthBiomeFilter<UserOverrideBiomeFilter.BiomeBoundingBox> {
    protected final BVH<BiomeBoundingBox> bvh;

    @SneakyThrows(IOException.class)
    public UserOverrideBiomeFilter(@NonNull GeographicProjection projection) {
        List<URL> configSources = new ArrayList<>();
        configSources.add(UserOverrideBiomeFilter.class.getResource("biome_overrides.json5"));

        try (Stream<Path> stream = Files.list(Files.createDirectories(Disk.configFile("biome_overrides")))) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().matches(".*\\.json5?$"))
                    .map(Path::toUri).map((EFunction<URI, URL>) URI::toURL)
                    .forEach(configSources::add);
        }

        this.bvh = BVH.of(configSources.stream()
                .map((IOFunction<URL, BiomeBoundingBox[]>) url -> TerraConstants.JSON_MAPPER.readValue(url, BiomeBoundingBox[].class))
                .flatMap(Arrays::stream)
                .toArray(BiomeBoundingBox[]::new));
    }

    @Override
    public CompletableFuture<BiomeBoundingBox> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        return CompletableFuture.supplyAsync(() -> this.bvh.getAllIntersecting(boundsGeo).stream()
                .max(Comparator.naturalOrder())
                .orElse(null));
    }

    @Override
    public void bake(ChunkPos pos, ChunkBiomesBuilder builder, BiomeBoundingBox bbox) {
        if (bbox == null) { //out of bounds, or no override at this position
            return;
        }

        if (bbox.replace == null) { //all biomes are overridden
            Arrays.fill(builder.state(), bbox.biome);
        } else {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (bbox.replace.contains(builder.get(x, z))) {
                        builder.set(x, z, bbox.biome);
                    }
                }
            }
        }
    }

    /**
     * Sets the biome in a specific bounding box.
     *
     * @author DaPorkchop_
     */
    @JsonDeserialize
    @JsonSerialize
    @Getter
    public static class BiomeBoundingBox implements Bounds2d, Comparable<BiomeBoundingBox> {
        protected final Set<Biome> replace;
        protected final Biome biome;

        protected final double minX;
        protected final double maxX;
        protected final double minZ;
        protected final double maxZ;

        @Getter(onMethod_ = { @JsonGetter })
        protected final double priority;

        @JsonCreator
        public BiomeBoundingBox(
                @JsonProperty(value = "replace", required = false) Biome[] replace,
                @JsonProperty(value = "biome", required = true) @NonNull Biome biome,
                @JsonProperty(value = "bounds", required = true) @NonNull Bounds2d bounds,
                @JsonProperty(value = "priority", defaultValue = "0.0") double priority) {
            this.replace = replace != null ? ImmutableSet.copyOf(replace) : null;
            this.biome = biome;
            this.priority = priority;

            this.minX = bounds.minX();
            this.maxX = bounds.maxX();
            this.minZ = bounds.minZ();
            this.maxZ = bounds.maxZ();
        }

        @Override
        public int compareTo(BiomeBoundingBox o) {
            return -Double.compare(this.priority, o.priority);
        }

        @JsonGetter("bounds")
        public Bounds2d bounds() {
            return Bounds2d.of(this.minX, this.maxX, this.minZ, this.maxZ);
        }
    }
}
