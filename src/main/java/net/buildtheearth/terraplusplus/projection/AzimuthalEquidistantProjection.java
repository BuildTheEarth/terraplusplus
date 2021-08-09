package net.buildtheearth.terraplusplus.projection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.buildtheearth.terraplusplus.TerraConstants;

import java.util.Map;

import static java.lang.Math.*;

/**
 * https://mathworld.wolfram.com/AzimuthalEquidistantProjection.html
 */
@JsonDeserialize
@Getter(onMethod_ = { @JsonGetter })
public class AzimuthalEquidistantProjection implements GeographicProjection {
    private static double modDegrees(double val, double bound) {
        if (val == bound * 0.5d) {
            return val;
        }

        val += bound * 0.5d;
        if (val < 0.0d) {
            val = val % bound + bound;
        }

        return val % bound - (bound * 0.5d);
    }

    private final double centerX;
    private final double centerY;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AzimuthalEquidistantProjection(
            @JsonProperty("centerX") Double centerX,
            @JsonProperty("centerY") Double centerY) {
        this.centerX = modDegrees(centerX != null ? centerX : 0.0d, 360.0d);
        this.centerY = modDegrees(centerY != null ? centerY : 0.0d, 180.0d);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        double phi1 = toRadians(this.centerY);
        double lambda0 = toRadians(this.centerX);

        double c = sqrt(x * x + y * y);
        if (c >= PI) {
            throw OutOfProjectionBoundsException.get();
        }

        double phi = asin(cos(c) * sin(phi1) + (y * sin(c) * cos(phi1) / c));
        double lambda;
        if (this.centerY == -90.0d) {
            lambda = lambda0 + atan2(x, y);
        } else if (this.centerY == 90.0d) {
            lambda = lambda0 + PI + atan2(-x, y);
        } else {
            lambda = lambda0 + atan2(x * sin(c), c * cos(phi1) * cos(c) - y * sin(phi1) * sin(c));
        }

        double longitude = -modDegrees(toDegrees(lambda), 360.0d);
        double latitude = toDegrees(phi);

        return new double[]{ longitude, latitude };
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        OutOfProjectionBoundsException.checkLongitudeLatitudeInRange(longitude, latitude);

        double phi1 = toRadians(this.centerY);
        double lambda0 = toRadians(this.centerX);
        double phi = toRadians(latitude);
        double lambda = -toRadians(longitude);

        double p = acos(sin(phi1) * sin(phi) + cos(phi1) * cos(phi) * cos(lambda - lambda0));
        double t = atan2(cos(phi) * sin(lambda - lambda0), cos(phi1) * sin(phi) - sin(phi1) * cos(phi) * cos(lambda - lambda0));

        double x = p * sin(t);
        double y = p * cos(t);

        return new double[]{ x, y };
    }

    @Override
    public double[] bounds() {
        return new double[]{ -PI, -PI, PI, PI };
    }

    @Override
    public double metersPerUnit() {
        return TerraConstants.EARTH_CIRCUMFERENCE / (2 * this.bounds()[2]);
    }

    @Override
    public String toString() {
        return "Azimuthal Equidistant";
    }

    @Override
    public Map<String, Object> properties() {
        return ImmutableMap.<String, Object>builder()
                .put("centerX", this.centerX)
                .put("centerY", this.centerY)
                .build();
    }
}
