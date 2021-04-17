package net.buildtheearth.terraplusplus.dataset.geojson.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Iterators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.buildtheearth.terraplusplus.dataset.geojson.GeoJsonObject;

import java.util.Iterator;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@ToString
@EqualsAndHashCode
@JsonDeserialize
@JsonTypeName("FeatureCollection")
public final class FeatureCollection implements GeoJsonObject, Iterable<Feature> {
    protected final Feature[] features;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FeatureCollection(
            @JsonProperty(value = "features", required = true) @NonNull Feature[] features) {
        this.features = features;
    }

    @Override
    public Iterator<Feature> iterator() {
        return Iterators.forArray(this.features);
    }
}
