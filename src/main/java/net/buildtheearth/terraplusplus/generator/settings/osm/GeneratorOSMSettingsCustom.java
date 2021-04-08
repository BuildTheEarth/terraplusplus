package net.buildtheearth.terraplusplus.generator.settings.osm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.osm.OSMMapper;

/**
 * Implementation of {@link GeneratorOSMSettings} which allows use of a custom, user-configured {@link OSMMapper}.
 *
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@JsonDeserialize
public final class GeneratorOSMSettingsCustom implements GeneratorOSMSettings {
    protected final OSMMapper<Geometry> mapper;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public GeneratorOSMSettingsCustom(
            @JsonProperty(value = "mapper", required = true) @NonNull OSMMapper<Geometry> mapper) {
        this.mapper = mapper;
    }
}
