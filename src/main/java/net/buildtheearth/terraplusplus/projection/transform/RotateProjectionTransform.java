package net.buildtheearth.terraplusplus.projection.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Getter;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

import static java.lang.Math.*;

/**
 * @author DaPorkchop_
 */
@JsonDeserialize
public class RotateProjectionTransform extends ProjectionTransform {
    @Getter(onMethod_ = { @JsonGetter })
    private final double by;

    private transient final double sin;
    private transient final double cos;
    private transient final double unsin;
    private transient final double uncos;

    /**
     * @param delegate - Input projection
     * @param by       - how much to rotate the projection by
     */
    @JsonCreator
    public RotateProjectionTransform(
            @JsonProperty(value = "delegate", required = true) GeographicProjection delegate,
            @JsonProperty(value = "by", required = true) double by) {
        super(delegate);
        Preconditions.checkArgument(Double.isFinite(by), "Projection rotation must be a finite double");
        this.by = by;

        this.sin = Math.sin(toRadians(-by));
        this.cos = Math.cos(toRadians(-by));
        this.unsin = Math.sin(toRadians(by));
        this.uncos = Math.cos(toRadians(by));
    }

    @Override
    public double[] bounds() {
        double[] bounds = super.bounds();

        double x0 = bounds[0] * this.cos - bounds[1] * this.sin;
        double x1 = bounds[0] * this.cos - bounds[3] * this.sin;
        double x2 = bounds[2] * this.cos - bounds[1] * this.sin;
        double x3 = bounds[2] * this.cos - bounds[3] * this.sin;

        double y0 = bounds[0] * this.sin + bounds[1] * this.cos;
        double y1 = bounds[0] * this.sin + bounds[3] * this.cos;
        double y2 = bounds[2] * this.sin + bounds[1] * this.cos;
        double y3 = bounds[2] * this.sin + bounds[3] * this.cos;

        return new double[] {
                min(min(x0, x1), min(x2, x3)),
                min(min(y0, y1), min(y2, y3)),
                max(max(x0, x1), max(x2, x3)),
                max(max(y0, y1), max(y2, y3))
        };
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return super.delegate.toGeo(
                x * this.uncos - y * this.unsin,
                x * this.unsin + y * this.uncos
        );
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] pos = super.delegate.fromGeo(longitude, latitude);
        return new double[] {
                pos[0] * this.cos - pos[1] * this.sin,
                pos[0] * this.sin + pos[1] * this.cos,
        };
    }

    @Override
    public String toString() {
        return "Rotate (" + super.delegate + ") by " + this.by + " degrees";
    }
}
