package net.buildtheearth.terraminusminus.projection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.buildtheearth.terraminusminus.TerraConstants;

import java.util.Map;

import static java.lang.Math.*;

/**
 * https://mathworld.wolfram.com/LambertAzimuthalEqual-AreaProjection.html
 */
@JsonDeserialize
@Getter(onMethod_ = { @JsonGetter })
public class LambertAzimuthalProjection implements GeographicProjection {
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
    public LambertAzimuthalProjection(
            @JsonProperty("centerX") Double centerX,
            @JsonProperty("centerY") Double centerY) {
        this.centerX = modDegrees(centerX != null ? centerX : 0.0d, 360.0d);
        this.centerY = modDegrees(centerY != null ? centerY : 0.0d, 180.0d);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        double phi1 = toRadians(this.centerY);
        double lambda0 = toRadians(this.centerX);

        double p = sqrt(x * x + y * y);
        double c = 2.0d * asin((1.0d / 2.0d) * p);

        if (Double.isNaN(c)) {
            throw OutOfProjectionBoundsException.get();
        }

        double phi = asin(cos(c) * sin(phi1) + (y * sin(c) * cos(phi1)) / p);
        double lambda = lambda0 + atan2(x * sin(c), p * cos(phi1) * cos(c) - y * sin(phi1) * sin(c));

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

        double k = sqrt(2.0d / (1.0d + sin(phi1) * sin(phi) + cos(phi1) * cos(phi) * cos(lambda - lambda0)));

        double x = k * cos(phi) * sin(lambda - lambda0);
        double y = k * (cos(phi1) * sin(phi) - sin(phi1) * cos(phi) * cos(lambda - lambda0));

        return new double[]{ x, y };
    }

    @Override
    public double[] bounds() {
        return new double[]{ -2.0d, -2.0d, 2.0d, 2.0d };
    }

    @Override
    public double metersPerUnit() {
        return TerraConstants.EARTH_CIRCUMFERENCE / (2 * this.bounds()[2]);
    }

    @Override
    public String toString() {
        return "Lambert Azimuthal";
    }

    @Override
    public Map<String, Object> properties() {
        return ImmutableMap.<String, Object>builder()
                .put("centerX", this.centerX)
                .put("centerY", this.centerY)
                .build();
    }
}
