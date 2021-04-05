package net.buildtheearth.terraplusplus.projection.mercator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.TerraUtils;

import java.util.Collections;
import java.util.Map;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Implementation of the web Mercator projection, with projected space normalized between 0 and 2^zoom * 256.
 * This projection is mainly used by tiled mapping services like GoogleMaps or OpenStreetMap.
 * In this implementation of the projection, 1 unit on the projected space corresponds to 1 pixel on those services at the same zoom level.
 * The origin is on the upper left corner of the map.
 *
 * @see CenteredMercatorProjection
 * @see <a href="https://en.wikipedia.org/wiki/Web_Mercator_projection"> Wikipedia's article on the Web Mercator projection</a>
 */
@JsonDeserialize
public class WebMercatorProjection implements GeographicProjection {

    public static final double LIMIT_LATITUDE = Math.toDegrees(2 * Math.atan(Math.pow(Math.E, Math.PI)) - Math.PI / 2);

    @Getter(onMethod_ = { @JsonGetter })
    protected final int zoom;

    protected transient final double scaleTo;
    protected transient final double scaleFrom;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public WebMercatorProjection(@JsonProperty("zoom") Integer zoom) {
        this.zoom = zoom != null ? notNegative(zoom, "zoom") : 0;

        this.scaleTo = 1.0d / (256 << this.zoom);
        this.scaleFrom = 256 << this.zoom;
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        if (x < 0 || y < 0 || x > this.scaleFrom || y > this.scaleFrom) {
            throw OutOfProjectionBoundsException.get();
        }
        return new double[]{
                Math.toDegrees(this.scaleTo * x * TerraUtils.TAU - Math.PI),
                Math.toDegrees(Math.atan(Math.exp(Math.PI - this.scaleTo * y * TerraUtils.TAU)) * 2 - Math.PI / 2)
        };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkInRange(longitude, latitude, 180, LIMIT_LATITUDE);
        return new double[]{
                this.scaleFrom * (Math.toRadians(longitude) + Math.PI) / TerraUtils.TAU,
                this.scaleFrom * (Math.PI - Math.log(Math.tan((Math.PI / 2 + Math.toRadians(latitude)) / 2))) / TerraUtils.TAU
        };
    }

    @Override
    public double[] bounds() {
        return new double[]{ 0, 0, this.scaleFrom, this.scaleFrom };
    }

    @Override
    public boolean upright() {
        return true;
    }

    @Override
    public double metersPerUnit() {
        return 100000;
    }

    @Override
    public String toString() {
        return "Web Mercator";
    }

    @Override
    public Map<String, Object> properties() {
        return Collections.singletonMap("zoom", this.zoom);
    }
}
