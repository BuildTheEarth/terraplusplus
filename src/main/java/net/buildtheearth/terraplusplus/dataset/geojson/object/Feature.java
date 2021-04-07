package net.buildtheearth.terraplusplus.dataset.geojson.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import net.buildtheearth.terraplusplus.dataset.geojson.GeoJsonObject;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@ToString
@EqualsAndHashCode
@JsonDeserialize
@JsonTypeName("Feature")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Feature implements GeoJsonObject {
    @NonNull
    protected final Geometry geometry;
    protected final Map<String, String> properties;
    protected final String id;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Feature(
            @JsonProperty(value = "geometry", required = true) @NonNull Geometry geometry,
            @JsonProperty("properties") Map<String, String> properties,
            @JsonProperty("id") String id) {
        this.geometry = geometry;
        this.properties = properties;
        this.id = id;
    }
}
