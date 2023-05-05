package net.buildtheearth.terraplusplus.generator.biome;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.generator.ChunkBiomesBuilder;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.TilePos;
import net.buildtheearth.terraplusplus.util.bvh.BVH;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.buildtheearth.terraplusplus.util.geo.pointarray.PointArray2D;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class BiomeFilterUserOverride implements IEarthBiomeFilter<BiomeFilterUserOverride.BiomeOverrideArea> {
    @Getter(onMethod_ = { @JsonGetter })
    protected final BiomeOverrideArea[] areas;
    protected final BVH<BiomeOverrideArea> bvh;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BiomeFilterUserOverride(
            @JsonProperty(value = "areas", required = true) @NonNull BiomeOverrideArea... areas) {
        this.areas = areas;
        this.bvh = BVH.of(areas);
    }

    @Override
    public CompletableFuture<BiomeOverrideArea> requestData(TilePos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo, PointArray2D sampledPoints) throws OutOfProjectionBoundsException {
        return CompletableFuture.supplyAsync(() -> this.bvh.getAllIntersecting(boundsGeo).stream()
                .max(Comparator.naturalOrder())
                .orElse(null));
    }

    @Override
    public void bake(TilePos pos, ChunkBiomesBuilder builder, BiomeOverrideArea bbox) {
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
    @Getter(onMethod_ = { @JsonGetter })
    @JsonDeserialize
    public static class BiomeOverrideArea implements Bounds2d, Comparable<BiomeOverrideArea> {
        protected final GeographicProjection projection;
        protected final Geometry geometry;

        protected final Set<Biome> replace;
        protected final Biome biome;

        protected final double priority;

        @Getter(AccessLevel.NONE)
        protected final Bounds2d bounds;

        @JsonCreator
        @SneakyThrows(OutOfProjectionBoundsException.class)
        public BiomeOverrideArea(
                @JsonProperty(value = "projection", required = true) @NonNull GeographicProjection projection,
                @JsonProperty(value = "geometry", required = true) @NonNull Geometry geometry,
                @JsonProperty(value = "replace", required = false) Set<Biome> replace,
                @JsonProperty(value = "biome", required = true) @JsonAlias({ "with" }) @NonNull Biome biome,
                @JsonProperty(value = "priority", required = false) double priority) {
            this.projection = projection;
            this.geometry = geometry;
            this.bounds = geometry.project(projection::toGeo).bounds();

            this.replace = replace != null ? ImmutableSet.copyOf(replace) : null;
            this.biome = biome;

            this.priority = priority;
        }

        @Override
        public int compareTo(BiomeOverrideArea o) {
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
}
