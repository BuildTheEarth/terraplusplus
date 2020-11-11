package io.github.terra121.projection.transform;

import io.github.terra121.projection.GeographicProjection;

public abstract class AbstractTransformation extends GeographicProjection {
    protected final GeographicProjection input;

    public AbstractTransformation(GeographicProjection input) {
        this.input = input;
    }

    @Override
    public boolean upright() {
        return this.input.upright();
    }

    @Override
    public double[] bounds() {
        return this.input.bounds();
    }

    @Override
    public double metersPerUnit() {
        return this.input.metersPerUnit();
    }
}
