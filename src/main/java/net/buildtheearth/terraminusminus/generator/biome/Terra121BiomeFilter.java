package net.buildtheearth.terraminusminus.generator.biome;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraminusminus.TerraConfig;
import net.buildtheearth.terraminusminus.dataset.IScalarDataset;
import net.buildtheearth.terraminusminus.generator.ChunkBiomesBuilder;
import net.buildtheearth.terraminusminus.generator.EarthBiomeProvider;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.GeneratorDatasets;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.substitutes.Biome;
import net.buildtheearth.terraminusminus.util.CornerBoundingBox2d;
import net.buildtheearth.terraminusminus.util.bvh.Bounds2d;

/**
 * Implementation of {@link IEarthBiomeFilter} that emits the same Biome as legacy terra121.
 *
 * @author DaPorkchop_
 */
public class Terra121BiomeFilter implements IEarthBiomeFilter<Terra121BiomeFilter.Data> {

    // Biomes used by this filter
    private static final Biome
            OCEAN = Biome.parse("ocean"),
            PLAINS = Biome.parse("plains"),
            DESERT = Biome.parse("desert"),
            FOREST = Biome.parse("forest"),
            TAIGA = Biome.parse("taiga"),
            SWAMP = Biome.parse("swamp"),
            SNOWY_PLAINS = Biome.parse("snowy_plains"),
            FROZEN_PEAKS = Biome.parse("frozen_peaks"),
            JUNGLE = Biome.parse("jungle"),
            SNOWY_TAIGA = Biome.parse("snowy_taiga"),
            SAVANNA = Biome.parse("savanna"),
            BADLANDS = Biome.parse("badlands");

    @Override
    public CompletableFuture<Terra121BiomeFilter.Data> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds, CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
        CompletableFuture<double[]> precipitationFuture = datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_TERRA121_PRECIPITATION).getAsync(boundsGeo, 16, 16);
        CompletableFuture<double[]> soilFuture = datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_TERRA121_SOIL).getAsync(boundsGeo, 16, 16);
        CompletableFuture<double[]> temperatureFuture = datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_TERRA121_TEMPERATURE).getAsync(boundsGeo, 16, 16);

        return CompletableFuture.allOf(precipitationFuture, soilFuture, temperatureFuture)
                .thenApply(unused -> new Data(precipitationFuture.join(), soilFuture.join(), temperatureFuture.join()));
    }

    @Override
    public void bake(ChunkPos pos, ChunkBiomesBuilder builder, Terra121BiomeFilter.Data data) {
        Biome[] biome = builder.state();

        if (data == null) {
            Arrays.fill(biome, TerraConfig.biomes.defaultBiome);
            return;
        }

        double[] precipitation = data.precipitation;
        double[] soil = data.soil;
        double[] temperature = data.temperature;

        for (int i = 0; i < 16 * 16; i++) {
            biome[i] = this.classify(precipitation[i], soil[i], temperature[i]);
        }
    }

    /**
     * This monstrosity of a piece of garbage is copied directly from the original terra121 implementation of {@link EarthBiomeProvider}.
     */
    protected Biome classify(double precipitation, double soil, double temperature) {
        switch ((int) soil) {
            case 0: //Ocean
                return OCEAN;
            case 1: //Shifting Sand
                return DESERT;
            case 2: //Rock
                return DESERT; //cant find it (rock mountians)
            case 3: //Ice
                return FROZEN_PEAKS;
            case 5:
            case 6:
            case 7: //Permafrost
                return SNOWY_PLAINS;
            case 10:
                return JUNGLE;
            case 11:
            case 12:
                return PLAINS;
            case 15:
                if (temperature < 5) {
                    return SNOWY_TAIGA;
                } else if (temperature > 15) {
                    return SWAMP;
                }
                return FOREST;
            case 16:
            case 17:
            case 18:
            case 19:
                if (temperature < 15) {
                    if (temperature < 0) {
                        return SNOWY_TAIGA;
                    }
                    return SWAMP;
                }
                if (temperature > 20) {
                    return SWAMP;
                }
                return FOREST;
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
                return SAVANNA;
            case 34:
                return JUNGLE;
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
                return PLAINS;
            case 50:
                return SNOWY_TAIGA;
            case 51: //salt flats always desert
                return DESERT;
            case 52:
            case 53:
            case 55:
            case 99: //hot and dry
                if (temperature < 2) {
                    return SNOWY_TAIGA;
                } else if (temperature < 5) {
                    return TAIGA;
                } else if (precipitation < 5) {
                    return DESERT;
                }
                return BADLANDS; //TODO: this soil can also be desert i.e. saudi Arabia (base on percip?)
            case 54:
            case 56:
                return SAVANNA;
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
                if (temperature < 10) {
                    return TAIGA;
                }
                return FOREST;
            case 70:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
                return PLAINS;
            case 13:
            case 40:
            case 71:
            case 80:
            case 95:
            case 98:
                return SWAMP;
            case 81:
            case 83:
            case 84:
            case 86:
                return FOREST;
            case 82:
            case 85:
                return PLAINS;
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
                return FOREST;
            case 96:
                return SAVANNA;
            case 97:
                return DESERT;
        }

        return PLAINS;
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
