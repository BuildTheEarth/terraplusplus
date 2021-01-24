package io.github.terra121.projection.transform;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.terra121.projection.GeographicProjection;

/**
 * Warps a Geographic projection and applies a transformation to it.
 */
public abstract class ProjectionTransform extends GeographicProjection {
    @JsonProperty
    protected final GeographicProjection delegate;

    /**
     * @param delegate - projection to transform
     */
    public ProjectionTransform(GeographicProjection delegate) {
        this.delegate = delegate;
    }

    @Override
    public void validate() throws IllegalStateException {
        super.validate();

        this.delegate.validate();
    }

    @Override
    public abstract GeographicProjection optimize();

    @Override
    public boolean upright() {
        return this.delegate.upright();
    }

    @Override
    public double[] bounds() {
        return this.delegate.bounds();
    }

    @Override
    public double metersPerUnit() {
        return this.delegate.metersPerUnit();
    }
}
