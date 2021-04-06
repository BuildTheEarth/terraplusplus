package net.buildtheearth.terraplusplus.projection.standard.epsg;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.buildtheearth.terraplusplus.config.ConstructDirectly;
import net.buildtheearth.terraplusplus.projection.mercator.WebMercatorProjection;

@JsonDeserialize
@JsonSerialize
@ConstructDirectly
public class EPSG3857Projection extends WebMercatorProjection {
    public EPSG3857Projection() {
        super(0);
    }

    @Override
    @JsonGetter
    public String toString() {
        return "EPSG:3857";
    }
}
