package io.github.terra121.projection;

/**
 * Scales the warps projection's projected space up or down.
 * More specifically, it multiplies x and y by there respective scale factors.
 */
public class ScaleProjection extends ProjectionTransform {

    double scaleX;
    double scaleY;

    /**
     * Creates a new ScaleProjection with different scale factor for the x and y axis.
     * 
     * @param input - projection to transform
     * @param scaleX - scaling to apply along the x axis
     * @param scaleY - scaling to apply along the y axis
     */
    public ScaleProjection(GeographicProjection input, double scaleX, double scaleY) {
        super(input);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /**
     * Creates a new ScaleProjection with the same scale factor for the x and y axis.
     * 
     * @param input - projection to transform
     * @param scale - scale factor to apply on both axis
     */
    public ScaleProjection(GeographicProjection input, double scale) {
        this(input, scale, scale);
    }

    @Override
    public double[] toGeo(double x, double y) {
        return this.input.toGeo(x / this.scaleX, y / this.scaleY);
    }

    @Override
    public double[] fromGeo(double lon, double lat) {
        double[] p = this.input.fromGeo(lon, lat);
        p[0] *= this.scaleX;
        p[1] *= this.scaleY;
        return p;
    }

    @Override
    public boolean upright() {
        return (this.scaleY < 0) ^ this.input.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.input.bounds();
        b[0] *= this.scaleX;
        b[1] *= this.scaleY;
        b[2] *= this.scaleX;
        b[3] *= this.scaleY;
        return b;
    }

    @Override
    public double metersPerUnit() {
        return this.input.metersPerUnit() / Math.sqrt((this.scaleX * this.scaleX + this.scaleY * this.scaleY) / 2); //TODO: better transform
    }
}
