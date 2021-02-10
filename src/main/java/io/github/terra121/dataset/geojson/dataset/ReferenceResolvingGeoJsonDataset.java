package io.github.terra121.dataset.geojson.dataset;

import io.github.terra121.dataset.geojson.GeoJsonObject;
import lombok.NonNull;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author DaPorkchop_
 */
public class ReferenceResolvingGeoJsonDataset extends AbstractReferenceResolvingGeoJsonDataset<GeoJsonObject[]> {
    public ReferenceResolvingGeoJsonDataset(@NonNull ParsingGeoJsonDataset delegate) {
        super(delegate);
    }

    @Override
    protected GeoJsonObject[] translate(@NonNull Stream<GeoJsonObject> inputs) {
        return inputs.toArray(GeoJsonObject[]::new);
    }

    @Override
    protected GeoJsonObject[] merge(@NonNull Stream<GeoJsonObject[]> inputs) {
        return inputs.flatMap(Arrays::stream).toArray(GeoJsonObject[]::new);
    }
}
