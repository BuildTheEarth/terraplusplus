package net.buildtheearth.terraplusplus.generator.biome;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.generator.ChunkBiomesBuilder;
import net.buildtheearth.terraplusplus.generator.EarthBiomeProvider;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.TilePos;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link IEarthBiomeFilter} that emits the same biomes as legacy terra121.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class BiomeFilterTerra121 implements IEarthBiomeFilter<BiomeFilterTerra121.Data> {
    @Override
    public CompletableFuture<BiomeFilterTerra121.Data> requestData(TilePos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        CompletableFuture<double[]> precipitationFuture = datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_TERRA121_PRECIPITATION).getAsync(boundsGeo, 16, 16);
        CompletableFuture<double[]> soilFuture = datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_TERRA121_SOIL).getAsync(boundsGeo, 16, 16);
        CompletableFuture<double[]> temperatureFuture = datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_TERRA121_TEMPERATURE).getAsync(boundsGeo, 16, 16);

        return CompletableFuture.allOf(precipitationFuture, soilFuture, temperatureFuture)
                .thenApply(unused -> new Data(precipitationFuture.join(), soilFuture.join(), temperatureFuture.join()));
    }

    @Override
    public void bake(TilePos pos, ChunkBiomesBuilder builder, BiomeFilterTerra121.Data data) {
        Biome[] biomes = builder.state();

        if (data == null) {
            Arrays.fill(biomes, Biomes.OCEAN);
            return;
        }

        double[] precipitation = data.precipitation;
        double[] soil = data.soil;
        double[] temperature = data.temperature;

        for (int i = 0; i < 16 * 16; i++) {
            biomes[i] = this.classify(precipitation[i], soil[i], temperature[i]);
        }
    }

    /**
     * This monstrosity of a piece of garbage is copied directly from the original terra121 implementation of {@link EarthBiomeProvider}.
     */
    protected Biome classify(double precipitation, double soil, double temperature) {
        switch ((int) soil) {
            case 0: //Ocean
                return Biomes.OCEAN;
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
                if (temperature < 5) {
                    return Biomes.COLD_TAIGA;
                } else if (temperature > 15) {
                    return Biomes.SWAMPLAND;
                }
                return Biomes.FOREST;
            case 16:
            case 17:
            case 18:
            case 19:
                if (temperature < 15) {
                    if (temperature < 0) {
                        return Biomes.COLD_TAIGA;
                    }
                    return Biomes.SWAMPLAND;
                }
                if (temperature > 20) {
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
                if (temperature < 2) {
                    return Biomes.COLD_TAIGA;
                } else if (temperature < 5) {
                    return Biomes.TAIGA; //TODO: Tundra in (1.15)
                } else if (precipitation < 5) {
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
                if (temperature < 10) {
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

    @RequiredArgsConstructor
    protected static class Data {
        @NonNull
        protected final double[] precipitation;
        @NonNull
        protected final double[] soil;
        @NonNull
        protected final double[] temperature;
    }
}
