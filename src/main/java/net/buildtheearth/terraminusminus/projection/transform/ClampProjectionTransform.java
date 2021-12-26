package net.buildtheearth.terraminusminus.projection.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Getter;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;

import java.util.Arrays;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
@Getter(onMethod_ = { @JsonGetter })
public class ClampProjectionTransform extends ProjectionTransform {
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    /**
     * @param delegate - Input projection
     */
    @JsonCreator
    public ClampProjectionTransform(
            @JsonProperty(value = "delegate", required = true) GeographicProjection delegate,
            @JsonProperty(value = "minX", required = true) double minX,
            @JsonProperty(value = "maxX", required = true) double maxX,
            @JsonProperty(value = "minY", required = true) double minY,
            @JsonProperty(value = "maxY", required = true) double maxY) {
        super(delegate);
        Preconditions.checkArgument(Double.isFinite(minX) && Double.isFinite(maxX) && Double.isFinite(minY) && Double.isFinite(maxY), "Projection bounds must be finite doubles");
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public double[] bounds() {
        return new double[] { this.minX, this.minY, this.maxX, this.maxY };
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        if (x < this.minX || x > this.maxX || y < this.minY || y > this.maxY) {
            throw OutOfProjectionBoundsException.get();
        }
        return super.delegate.toGeo(x, y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] pos = super.delegate.fromGeo(longitude, latitude);
        if (pos[0] < this.minX || pos[0] > this.maxX || pos[1] < this.minY || pos[1] > this.maxY) {
            throw OutOfProjectionBoundsException.get();
        }
        return pos;
    }

    @Override
    public String toString() {
        return "Clamp (" + super.delegate + ") to " + Arrays.toString(this.bounds());
    }
}
