package net.buildtheearth.terraplusplus.dataset.geojson.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.buildtheearth.terraplusplus.dataset.geojson.GeoJsonObject;

/**
 * Non-standard GeoJSON object: represents a reference to another URL containing a GeoJSON object that this object should be substituted with.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@ToString
@EqualsAndHashCode
@JsonDeserialize
@JsonTypeName("Reference")
public final class Reference implements GeoJsonObject {
    protected final String location;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Reference(
            @JsonProperty(value = "location", required = true) @NonNull String location) {
        this.location = location;
    }
}
