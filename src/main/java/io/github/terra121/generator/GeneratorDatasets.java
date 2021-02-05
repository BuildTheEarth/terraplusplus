package io.github.terra121.generator;

import io.github.terra121.dataset.osm.OpenStreetMap;
import io.github.terra121.dataset.scalar.MultiresScalarDataset;
import io.github.terra121.dataset.scalar.ScalarDataset;
import io.github.terra121.event.InitDatasetsEvent;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.CustomAttributeContainer;
import lombok.Getter;
import lombok.NonNull;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;

/**
 * Wrapper class which contains all of the datasets used by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
@Getter
public class GeneratorDatasets extends CustomAttributeContainer<Object> {
    protected static Map<String, Object> getCustomDatasets(@NonNull EarthGeneratorSettings settings) {
        InitDatasetsEvent event = new InitDatasetsEvent(settings);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.getAllCustomProperties();
    }

    protected final GeographicProjection projection;

    protected final ScalarDataset heights;
    protected final ScalarDataset trees;
    protected final OpenStreetMap osm;

    public GeneratorDatasets(@NonNull EarthGeneratorSettings settings) {
        super(getCustomDatasets(settings));

        this.projection = settings.projection();

        this.heights = new MultiresScalarDataset("heights", settings.useDefaultHeights());
        this.trees = new MultiresScalarDataset("trees", settings.useDefaultTrees());
        this.osm = new OpenStreetMap(settings);
    }
}
