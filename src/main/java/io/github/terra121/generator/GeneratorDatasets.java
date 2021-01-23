package io.github.terra121.generator;

import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.dataset.impl.Heights;
import io.github.terra121.dataset.impl.Trees;
import io.github.terra121.dataset.osm.OpenStreetMap;
import io.github.terra121.event.InitDatasetsEvent;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.CustomAttributeContainer;
import lombok.Getter;
import lombok.NonNull;
import net.minecraftforge.common.MinecraftForge;

/**
 * Wrapper class which contains all of the datasets used by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
@Getter
public class GeneratorDatasets extends CustomAttributeContainer<Object> {
    protected final GeographicProjection projection;

    protected final ScalarDataset heights;
    protected final OpenStreetMap osm;
    protected final ScalarDataset trees;

    public GeneratorDatasets(@NonNull GeographicProjection projection, @NonNull EarthGeneratorSettings settings) {
        this.projection = projection;

        this.osm = new OpenStreetMap(projection);
        this.heights = Heights.constructDataset(settings.settings.smoothblend ? BlendMode.SMOOTH : BlendMode.LINEAR);
        this.trees = new Trees();

        InitDatasetsEvent event = new InitDatasetsEvent(projection, settings);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        super.custom = event.getAllCustomProperties();
    }
}
