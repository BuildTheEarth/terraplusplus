package net.buildtheearth.terraplusplus.generator;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.TerraConfig;
import net.buildtheearth.terraplusplus.dataset.builtin.Climate;
import net.buildtheearth.terraplusplus.dataset.builtin.Soil;
import net.buildtheearth.terraplusplus.dataset.geojson.dataset.ParsingGeoJsonDataset;
import net.buildtheearth.terraplusplus.dataset.geojson.dataset.ReferenceResolvingGeoJsonDataset;
import net.buildtheearth.terraplusplus.dataset.geojson.dataset.TiledGeoJsonDataset;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.scalar.MultiScalarDataset;
import net.buildtheearth.terraplusplus.dataset.scalar.ScalarDatasetConfigurationParser;
import net.buildtheearth.terraplusplus.dataset.vector.GeoJsonToVectorDataset;
import net.buildtheearth.terraplusplus.dataset.vector.VectorTiledDataset;
import net.buildtheearth.terraplusplus.event.InitDatasetsEvent;
import net.buildtheearth.terraplusplus.event.InitEarthRegistryEvent;
import net.buildtheearth.terraplusplus.generator.biome.IEarthBiomeFilter;
import net.buildtheearth.terraplusplus.generator.biome.Terra121BiomeFilter;
import net.buildtheearth.terraplusplus.generator.biome.UserOverrideBiomeFilter;
import net.buildtheearth.terraplusplus.generator.data.HeightsBaker;
import net.buildtheearth.terraplusplus.generator.data.IEarthDataBaker;
import net.buildtheearth.terraplusplus.generator.data.InitialBiomesBaker;
import net.buildtheearth.terraplusplus.generator.data.NullIslandBaker;
import net.buildtheearth.terraplusplus.generator.data.OSMBaker;
import net.buildtheearth.terraplusplus.generator.data.TreeCoverBaker;
import net.buildtheearth.terraplusplus.generator.populate.BiomeDecorationPopulator;
import net.buildtheearth.terraplusplus.generator.populate.CompatibilityEarthPopulators;
import net.buildtheearth.terraplusplus.generator.populate.IEarthPopulator;
import net.buildtheearth.terraplusplus.generator.populate.SnowPopulator;
import net.buildtheearth.terraplusplus.generator.populate.TreePopulator;
import net.buildtheearth.terraplusplus.util.OrderedRegistry;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.stream.Stream;

import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * Default processing pipelines for various earth generator processing steps.
 *
 * @author DaPorkchop_
 */
@UtilityClass
public class EarthGeneratorPipelines {
    public final String KEY_DATASET_HEIGHTS = "heights";
    public final String KEY_DATASET_OSM_RAW = "osm_raw";
    public final String KEY_DATASET_OSM_PARSED = "osm_parsed";
    public final String KEY_DATASET_TERRA121_PRECIPITATION = "terra121_precipitation";
    public final String KEY_DATASET_TERRA121_SOIL = "terra121_soil";
    public final String KEY_DATASET_TERRA121_TEMPERATURE = "terra121_temperature";
    public final String KEY_DATASET_TREE_COVER = "tree_cover";

    public final String KEY_DATA_TREE_COVER = "tree_cover";

    private <T> T[] fire(@NonNull InitEarthRegistryEvent<T> event) {
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.registry().entryStream().map(Map.Entry::getValue).toArray(i -> uncheckedCast(Array.newInstance(event.getGenericType(), i)));
    }

    public Map<String, Object> datasets(@NonNull EarthGeneratorSettings settings) {
        InitDatasetsEvent event = new InitDatasetsEvent(settings);

        event.register(KEY_DATASET_HEIGHTS, ScalarDatasetConfigurationParser.loadAndMerge(Stream.<String[]>of(TerraConfig.elevation.servers)).join());

        ParsingGeoJsonDataset rawOsm = new ParsingGeoJsonDataset(TerraConfig.openstreetmap.servers);
        event.register(KEY_DATASET_OSM_RAW, new TiledGeoJsonDataset(new ReferenceResolvingGeoJsonDataset(rawOsm)));
        event.register(KEY_DATASET_OSM_PARSED, new VectorTiledDataset(new GeoJsonToVectorDataset(rawOsm, OSMMapper.load(), settings.projection())));

        event.register(KEY_DATASET_TERRA121_PRECIPITATION, new Climate.Precipitation());
        event.register(KEY_DATASET_TERRA121_SOIL, new Soil());
        event.register(KEY_DATASET_TERRA121_TEMPERATURE, new Climate.Temperature());
        //event.register(KEY_DATASET_TREE_COVER, new MultiScalarDataset(KEY_DATASET_TREE_COVER, settings.useDefaultTreeCover()));
        event.register(KEY_DATASET_TREE_COVER, new Soil()); //TODO: remove this

        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.getAllCustomProperties();
    }

    public IEarthBiomeFilter<?>[] biomeFilters(@NonNull EarthGeneratorSettings settings) {
        return fire(new InitEarthRegistryEvent<IEarthBiomeFilter>(settings,
                uncheckedCast(new OrderedRegistry<IEarthBiomeFilter<?>>()
                        .addLast("legacy_terra121", new Terra121BiomeFilter())
                        .addLast("biome_overrides", new UserOverrideBiomeFilter(settings.projection())))) {});
    }

    public IEarthDataBaker<?>[] dataBakers(@NonNull EarthGeneratorSettings settings) {
        return fire(new InitEarthRegistryEvent<IEarthDataBaker>(settings,
                uncheckedCast(new OrderedRegistry<IEarthDataBaker<?>>()
                        .addLast("initial_biomes", new InitialBiomesBaker(settings.biomeProvider()))
                        .addLast("tree_cover", new TreeCoverBaker())
                        .addLast("heights", new HeightsBaker())
                        .addLast("osm", new OSMBaker())
                        .addLast("null_island", new NullIslandBaker()))) {});
    }

    public IEarthPopulator[] populators(@NonNull EarthGeneratorSettings settings) {
        return fire(new InitEarthRegistryEvent<IEarthPopulator>(settings,
                new OrderedRegistry<IEarthPopulator>()
                        .addLast("fml_pre_cube_populate_event", CompatibilityEarthPopulators.cubePopulatePre())
                        .addLast("trees", new TreePopulator())
                        .addLast("biome_decorate", new BiomeDecorationPopulator(settings))
                        .addLast("snow", new SnowPopulator())
                        .addLast("fml_post_cube_populate_event", CompatibilityEarthPopulators.cubePopulatePost())
                        .addLast("cc_cube_generators_registry", CompatibilityEarthPopulators.cubeGeneratorsRegistry())) {});
    }
}
