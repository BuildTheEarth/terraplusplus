package net.buildtheearth.terraminusminus.generator;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraminusminus.TerraConfig;
import net.buildtheearth.terraminusminus.dataset.builtin.Climate;
import net.buildtheearth.terraminusminus.dataset.builtin.Soil;
import net.buildtheearth.terraminusminus.dataset.geojson.dataset.ParsingGeoJsonDataset;
import net.buildtheearth.terraminusminus.dataset.geojson.dataset.ReferenceResolvingGeoJsonDataset;
import net.buildtheearth.terraminusminus.dataset.geojson.dataset.TiledGeoJsonDataset;
import net.buildtheearth.terraminusminus.dataset.osm.OSMMapper;
import net.buildtheearth.terraminusminus.dataset.scalar.MultiScalarDataset;
import net.buildtheearth.terraminusminus.dataset.vector.GeoJsonToVectorDataset;
import net.buildtheearth.terraminusminus.dataset.vector.VectorTiledDataset;
import net.buildtheearth.terraminusminus.generator.biome.IEarthBiomeFilter;
import net.buildtheearth.terraminusminus.generator.biome.Terra121BiomeFilter;
import net.buildtheearth.terraminusminus.generator.data.HeightsBaker;
import net.buildtheearth.terraminusminus.generator.data.IEarthDataBaker;
import net.buildtheearth.terraminusminus.generator.data.InitialBiomesBaker;
import net.buildtheearth.terraminusminus.generator.data.NullIslandBaker;
import net.buildtheearth.terraminusminus.generator.data.OSMBaker;
import net.buildtheearth.terraminusminus.generator.data.TreeCoverBaker;

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

    public Map<String, Object> datasets(@NonNull EarthGeneratorSettings settings) {
        Map<String, Object> m = new HashMap<>();

        m.put(KEY_DATASET_HEIGHTS, new MultiScalarDataset(KEY_DATASET_HEIGHTS, settings.useDefaultHeights()));

        ParsingGeoJsonDataset rawOsm = new ParsingGeoJsonDataset(TerraConfig.openstreetmap.servers);
        m.put(KEY_DATASET_OSM_RAW, new TiledGeoJsonDataset(new ReferenceResolvingGeoJsonDataset(rawOsm)));
        m.put(KEY_DATASET_OSM_PARSED, new VectorTiledDataset(new GeoJsonToVectorDataset(rawOsm, OSMMapper.load(), settings.projection())));

        m.put(KEY_DATASET_TERRA121_PRECIPITATION, new Climate.Precipitation());
        m.put(KEY_DATASET_TERRA121_SOIL, new Soil());
        m.put(KEY_DATASET_TERRA121_TEMPERATURE, new Climate.Temperature());
        m.put(KEY_DATASET_TREE_COVER, new MultiScalarDataset(KEY_DATASET_TREE_COVER, settings.useDefaultTreeCover()));

        return m;
    }

    public IEarthBiomeFilter<?>[] biomeFilters(@NonNull EarthGeneratorSettings settings) {
        return new IEarthBiomeFilter<?>[] {new Terra121BiomeFilter() };
    }

    public IEarthDataBaker<?>[] dataBakers(@NonNull EarthGeneratorSettings settings) {
        return new IEarthDataBaker<?>[] {
            new InitialBiomesBaker(settings.biomeProvider()),
            new TreeCoverBaker(),
            new HeightsBaker(),
            new OSMBaker(),
            new NullIslandBaker()
        };
    }
}
