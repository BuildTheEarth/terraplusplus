package net.buildtheearth.terraplusplus.projection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.TerraConstants;

import java.util.Map;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * https://mathworld.wolfram.com/StereographicProjection.html
 */
@JsonDeserialize
@Getter(onMethod_ = { @JsonGetter })
public class StereographicProjection implements GeographicProjection {
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
    private final double radius;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StereographicProjection(
            @JsonProperty("centerX") Double centerX,
            @JsonProperty("centerY") Double centerY,
            @JsonProperty("radius") Double radius) {
        this.centerX = modDegrees(centerX != null ? centerX : 0.0d, 360.0d);
        this.centerY = modDegrees(centerY != null ? centerY : 0.0d, 180.0d);
        this.radius = radius != null ? radius : 90.0d;

        checkArg(this.radius > 0.0d, "radius must be positive!");
        checkArg(this.radius < 180.0d, "radius must be less than 180!");
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        double phi1 = toRadians(this.centerY);
        double lambda0 = toRadians(this.centerX);

        double p = sqrt(x * x + y * y);
        double c = 2.0d * atan2(p, 2.0d);

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

        double phi = toRadians(latitude - this.centerY);
        double lambda = -toRadians(longitude + this.centerX);

        double k = 2.0d / (1.0d + cos(phi) * cos(lambda));

        double x = k * cos(phi) * sin(lambda);
        double y = k * sin(phi);

        return new double[]{ x, y };
    }

    @Override
    public double[] bounds() {
        double lambda = toRadians(this.radius);
        double k = 2.0d / (1.0d + cos(lambda));
        double r = abs(k * sin(lambda));

        return new double[]{ -r, -r, r, r };
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
                .put("radius", this.radius)
                .build();
    }
}
