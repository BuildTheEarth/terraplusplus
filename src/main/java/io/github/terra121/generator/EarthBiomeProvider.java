package io.github.terra121.generator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.terra121.dataset.builtin.Climate;
import io.github.terra121.dataset.builtin.Soil;
import io.github.terra121.generator.biome.IEarthBiomeFilter;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.ImmutableCompactArray;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class EarthBiomeProvider extends BiomeProvider {
    public final Soil soil = new Soil();
    public final Climate climate = new Climate();

    @NonNull
    public final GeographicProjection projection;

    protected final LoadingCache<ChunkPos, CompletableFuture<ImmutableCompactArray<Biome>>> cache;

    public EarthBiomeProvider(@NonNull EarthGeneratorSettings settings) {
        this.projection = settings.projection();

        this.cache = CacheBuilder.newBuilder()
                .weakValues()
                .build(new ChunkDataLoader(settings));
    }

    /**
     * Returns the biome generator based on soil and climate (mostly soil)
     */
    @Override
    public Biome getBiome(BlockPos pos) {
        //null island
        if (-80 < pos.getX() && pos.getX() < 80 && -80 < pos.getZ() && pos.getZ() < 80) {
            if (-16 < pos.getX() && pos.getX() < 16 && -16 < pos.getZ() && pos.getZ() < 16) {
                return Biomes.FOREST;
            }
            return Biomes.PLAINS;
        }

        try {
            return this.classify(this.projection.toGeo(pos.getX(), pos.getZ()));
        } catch (OutOfProjectionBoundsException e) { //out of bounds, assume ocean
            return Biomes.DEEP_OCEAN;
        }
    }

    /**
     * Get explicit data on the environment (soil, tempature, precipitation)
     */
    public double[] getEnv(double lon, double lat) {
        Climate.ClimateData clim = this.climate.getPoint(lon, lat);

        return new double[]{ this.soil.getPoint(lon, lat),
                clim.temp, clim.precip };
    }

    public Biome classify(double[] projected) {

        Climate.ClimateData clim = this.climate.getPoint(projected[0], projected[1]);
        byte stype = this.soil.getPoint(projected[0], projected[1]);

        switch (stype) {
            case 0: //Ocean
                if (clim.temp < -5) {
                    return Biomes.FROZEN_OCEAN;
                }
                return Biomes.DEEP_OCEAN;
            case 1: //Shifting Sand
                return Biomes.DESERT;
            case 2: //Rock
                return Biomes.DESERT; //cant find it (rock mountians)
            case 3: //Ice
                return Biomes.ICE_MOUNTAINS;

            case 5:
            case 6:
            case 7: //Permafrost
                return Biomes.ICE_PLAINS;
            case 10:
                return Biomes.JUNGLE;
            case 11:
            case 12:
                return Biomes.PLAINS;

            case 15:
                if (clim.temp < 5) {
                    return Biomes.COLD_TAIGA;
                } else if (clim.temp > 15) {
                    return Biomes.SWAMPLAND;
                }
                return Biomes.FOREST;

            case 16:
            case 17:
            case 18:
            case 19:
                if (clim.temp < 15) {
                    if (clim.temp < 0) {
                        return Biomes.COLD_TAIGA;
                    }
                    return Biomes.SWAMPLAND;
                }
                if (clim.temp > 20) {
                    return Biomes.SWAMPLAND;
                }
                return Biomes.FOREST;

            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
                return Biomes.SAVANNA;
            case 34:
                return Biomes.JUNGLE;
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
                return Biomes.PLAINS;

            case 50:
                return Biomes.COLD_TAIGA;
            case 51: //salt flats always desert
                return Biomes.DESERT;
            case 52:
            case 53:
            case 55:
            case 99: //hot and dry
                if (clim.temp < 2) {
                    return Biomes.COLD_TAIGA;
                }
                if (clim.temp < 5) {
                    return Biomes.TAIGA; //TODO: Tundra in (1.15)
                }
                if (clim.precip < 5) {
                    return Biomes.DESERT;
                }
                return Biomes.MESA; //TODO: this soil can also be desert i.e. saudi Arabia (base on percip?)

            case 54:
            case 56:
                return Biomes.SAVANNA;

            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
                if (clim.temp < 10) {
                    return Biomes.TAIGA;
                }
                return Biomes.FOREST;

            case 70:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
                return Biomes.PLAINS;

            case 13:
            case 40:
            case 71:
            case 80:
            case 95:
            case 98:
                return Biomes.SWAMPLAND;

            case 81:
            case 83:
            case 84:
            case 86:
                return Biomes.FOREST;
            case 82:
            case 85:
                return Biomes.PLAINS;

            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
                return Biomes.FOREST;
            case 96:
                return Biomes.SAVANNA;
            case 97:
                return Biomes.DESERT;
        }

        return Biomes.PLAINS;
    }

    /**
     * Returns an array of biomes for the location input.
     */
    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }

        Arrays.fill(biomes, 0, width * height, Biomes.FOREST);
        return biomes;
    }

    /**
     * Gets biomes to use for the blocks and loads the other data like temperature and humidity onto the
     * WorldChunkManager.
     */
    @Override
    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        if (oldBiomeList == null || oldBiomeList.length < width * depth) {
            oldBiomeList = new Biome[width * depth];
        }

        for (int r = 0; r < width; r++) {
            for (int c = 0; c < depth; c++) {
                oldBiomeList[r * depth + c] = this.getBiome(new BlockPos(x + r, 0, z + c));
            }
        }
        return oldBiomeList;
    }

    /**
     * Gets a list of biomes for the specified blocks.
     */
    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.getBiomes(listToReuse, x, z, width, length);
    }

    @Override
    @Nullable
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return null;
    }

    /**
     * checks given Chunk's Biomes against List of allowed ones
     */
    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        return true;
    }

    public static class ChunkDataLoader extends CacheLoader<ChunkPos, CompletableFuture<ImmutableCompactArray<Biome>>> {
        protected final GeneratorDatasets datasets;
        protected final IEarthBiomeFilter<?>[] filters;

        public ChunkDataLoader(@NonNull EarthGeneratorSettings settings) {
            this.datasets = settings.datasets();
            this.filters = EarthGeneratorPipelines.biomeFilters(settings);
        }

        @Override
        public CompletableFuture<ImmutableCompactArray<Biome>> load(@NonNull ChunkPos pos) {
            try {
                return IEarthAsyncPipelineStep.getFuture(pos, this.datasets, this.filters, ChunkBiomesBuilder::get);
            } catch (OutOfProjectionBoundsException e) {
                return CompletableFuture.completedFuture(ChunkBiomesBuilder.BLANK);
            }
        }
    }
}