package io.github.terra121.projection.mercator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.util.MathUtils;
import lombok.Getter;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Implementation of the web Mercator projection, with projected space normalized between 0 and 256.
 * The Greenwich meridian and the equator are at x=0 and y=0 respectively.
 *
 * @see CenteredMercatorProjection
 * @see <a href="https://en.wikipedia.org/wiki/Web_Mercator_projection"> Wikipedia's article on the Web Mercator projection</a>
 */
@JsonDeserialize
public class WebMercatorProjection implements GeographicProjection {
    @Getter(onMethod_ = { @JsonGetter })
    protected final int zoom;

    protected transient final double scaleTo;
    protected transient final double scaleFrom;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public WebMercatorProjection(@JsonProperty(value = "zoom", required = true) int zoom) {
        this.zoom = notNegative(zoom, "zoom");

        this.scaleTo = 1.0d / (256 << zoom);
        this.scaleFrom = 256 << zoom;
    }

    @Override
    public double[] toGeo(double x, double y) {
        return new double[]{
                Math.toDegrees(this.scaleTo * (x * MathUtils.TAU - Math.PI)),
                Math.toDegrees(this.scaleTo * (Math.atan(Math.exp(Math.PI - y * MathUtils.TAU)) * 2 - Math.PI / 2))
        };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) {
        return new double[]{
                this.scaleFrom * (Math.toRadians(longitude) + Math.PI) / MathUtils.TAU,
                this.scaleFrom * (Math.PI - Math.log(Math.tan((Math.PI / 2 + Math.toRadians(latitude)) / 2))) / MathUtils.TAU
        };
    }

    @Override
    public double[] bounds() {
        return new double[]{ 0, 0, 1, 1 };
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
}
