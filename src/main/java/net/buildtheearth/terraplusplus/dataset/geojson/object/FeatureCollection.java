package net.buildtheearth.terraplusplus.dataset.geojson.object;

import com.google.common.collect.Iterators;
import lombok.Data;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.GeoJsonObject;

import java.util.Iterator;

/**
 * @author DaPorkchop_
 */
@Data
public final class FeatureCollection implements GeoJsonObject, Iterable<Feature> {
    @NonNull
    protected final Feature[] features;

    @Override
    public Iterator<Feature> iterator() {
        return Iterators.forArray(this.features);
    }
}
