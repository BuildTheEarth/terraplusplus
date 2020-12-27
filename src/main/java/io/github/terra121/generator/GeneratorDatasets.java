package io.github.terra121.generator;

import io.github.terra121.dataset.BlendMode;
import io.github.terra121.dataset.ScalarDataset;
import io.github.terra121.dataset.impl.Heights;
import io.github.terra121.dataset.impl.Trees;
import io.github.terra121.dataset.osm.OpenStreetMap;
import io.github.terra121.projection.GeographicProjection;
import lombok.NonNull;

/**
 * Wrapper class which contains all of the datasets used by {@link EarthGenerator}.
 *
 * @author DaPorkchop_
 */
public class GeneratorDatasets {
    public final GeographicProjection projection;

    public final ScalarDataset heights;
    public final OpenStreetMap osm;
    public final ScalarDataset trees;

    public GeneratorDatasets(@NonNull GeographicProjection projection,  @NonNull EarthGeneratorSettings cfg, boolean features) {
        this.projection = projection;

        this.osm = new OpenStreetMap(projection, features && cfg.settings.roads, cfg.settings.osmwater, features && cfg.settings.buildings);
        this.heights = Heights.constructDataset(cfg.settings.smoothblend ? BlendMode.SMOOTH : BlendMode.LINEAR);
        this.trees = new Trees();
    }
}
