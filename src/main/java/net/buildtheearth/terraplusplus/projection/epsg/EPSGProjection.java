package net.buildtheearth.terraplusplus.projection.epsg;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;

/**
 * Base implementation of an EPSG projection.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@JsonSerialize
public abstract class EPSGProjection implements GeographicProjection {
    protected final int code;

    @Override
    @JsonValue
    public String toString() {
        return "EPSG:" + this.code;
    }
}
