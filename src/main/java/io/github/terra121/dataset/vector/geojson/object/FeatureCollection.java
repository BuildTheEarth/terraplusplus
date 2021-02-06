package io.github.terra121.dataset.vector.geojson.object;

import com.google.common.collect.Iterators;
import io.github.terra121.dataset.vector.geojson.GeoJSONObject;
import lombok.Data;
import lombok.NonNull;

import java.util.Iterator;

/**
 * @author DaPorkchop_
 */
@Data
public final class FeatureCollection implements GeoJSONObject, Iterable<Feature> {
    @NonNull
    protected final Feature[] features;

    @Override
    public Iterator<Feature> iterator() {
        return Iterators.forArray(this.features);
    }
}
