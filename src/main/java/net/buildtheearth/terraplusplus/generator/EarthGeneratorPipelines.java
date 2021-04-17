package net.buildtheearth.terraplusplus.generator;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.TerraConfig;
import net.buildtheearth.terraplusplus.dataset.IElementDataset;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.dataset.builtin.Climate;
import net.buildtheearth.terraplusplus.dataset.builtin.Soil;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.dataset.ParsingGeoJsonDataset;
import net.buildtheearth.terraplusplus.dataset.geojson.dataset.ReferenceResolvingGeoJsonDataset;
import net.buildtheearth.terraplusplus.dataset.geojson.dataset.TiledGeoJsonDataset;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;
import net.buildtheearth.terraplusplus.dataset.scalar.ScalarDatasetConfigurationParser;
import net.buildtheearth.terraplusplus.dataset.vector.GeoJsonToVectorDataset;
import net.buildtheearth.terraplusplus.dataset.vector.VectorTiledDataset;
import net.buildtheearth.terraplusplus.event.InitDatasetsEvent;
import net.buildtheearth.terraplusplus.event.InitEarthRegistryEvent;
import net.buildtheearth.terraplusplus.generator.biome.IEarthBiomeFilter;
import net.buildtheearth.terraplusplus.generator.data.IEarthDataBaker;
import net.buildtheearth.terraplusplus.generator.populate.CompatibilityEarthPopulators;
import net.buildtheearth.terraplusplus.generator.populate.IEarthPopulator;
import net.buildtheearth.terraplusplus.util.OrderedRegistry;
import net.buildtheearth.terraplusplus.util.bvh.BVH;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

        //start loading both datasets at once to reduce blocking time
        CompletableFuture<IScalarDataset> elevationFuture = ScalarDatasetConfigurationParser.loadAndMergeDatasetsFromManifests(settings.useDefaultHeights()
                ? Stream.concat(Stream.<String[]>of(TerraConfig.elevation.servers), Stream.of(settings.customHeights()))
                : Stream.of(settings.customHeights()));
        CompletableFuture<IScalarDataset> treeCoverFuture = ScalarDatasetConfigurationParser.loadAndMergeDatasetsFromManifests(settings.useDefaultTreeCover()
                ? Stream.concat(Stream.<String[]>of(TerraConfig.treeCover.servers), Stream.of(settings.customTreeCover()))
                : Stream.of(settings.customTreeCover()));
        event.register(KEY_DATASET_HEIGHTS, elevationFuture.join());
        event.register(KEY_DATASET_TREE_COVER, treeCoverFuture.join());

        ParsingGeoJsonDataset rawOsm = new ParsingGeoJsonDataset(TerraConfig.openstreetmap.servers);
        event.register(KEY_DATASET_OSM_RAW, new TiledGeoJsonDataset(new ReferenceResolvingGeoJsonDataset(rawOsm)));
        OSMMapper<Geometry> osmMapper = settings.osmSettings().mapper();
        event.register(KEY_DATASET_OSM_PARSED, osmMapper != null
                ? new VectorTiledDataset(new GeoJsonToVectorDataset(rawOsm, osmMapper, settings.projection()))
                : IElementDataset.empty(BVH.class));

        event.register(KEY_DATASET_TERRA121_PRECIPITATION, new Climate.Precipitation());
        event.register(KEY_DATASET_TERRA121_SOIL, new Soil());
        event.register(KEY_DATASET_TERRA121_TEMPERATURE, new Climate.Temperature());

        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.getAllCustomProperties();
    }

    public IEarthBiomeFilter<?>[] biomeFilters(@NonNull EarthGeneratorSettings settings) {
        OrderedRegistry<IEarthBiomeFilter<?>> registry = new OrderedRegistry<>();
        settings.biomeFilters().forEach(b -> registry.addLast(b.typeId(), b));

        return fire(new InitEarthRegistryEvent<IEarthBiomeFilter>(settings, uncheckedCast(registry)) {});
    }

    public IEarthDataBaker<?>[] dataBakers(@NonNull EarthGeneratorSettings settings) {
        OrderedRegistry<IEarthDataBaker<?>> registry = new OrderedRegistry<>();
        settings.dataBakers().forEach(b -> registry.addLast(b.typeId(), b));

        return fire(new InitEarthRegistryEvent<IEarthDataBaker>(settings, uncheckedCast(registry)) {});
    }

    public IEarthPopulator[] populators(@NonNull EarthGeneratorSettings settings) {
        OrderedRegistry<IEarthPopulator> registry = new OrderedRegistry<>();
        settings.populators().forEach(b -> registry.addLast(b.typeId(), b));

        registry.addLast("fml_pre_cube_populate_event", CompatibilityEarthPopulators.cubePopulatePre())
                .addLast("fml_post_cube_populate_event", CompatibilityEarthPopulators.cubePopulatePost())
                .addLast("cc_cube_generators_registry", CompatibilityEarthPopulators.cubeGeneratorsRegistry());

        return fire(new InitEarthRegistryEvent<IEarthPopulator>(settings, uncheckedCast(registry)) {});
    }
}
