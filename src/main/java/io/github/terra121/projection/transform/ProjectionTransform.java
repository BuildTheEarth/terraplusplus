package io.github.terra121.projection.transform;

import com.fasterxml.jackson.annotation.JsonGetter;
import io.github.terra121.projection.GeographicProjection;
import lombok.Getter;

/**
 * Warps a Geographic projection and applies a transformation to it.
 */
@Getter(onMethod_ = { @JsonGetter })
public abstract class ProjectionTransform extends GeographicProjection {
    protected final GeographicProjection delegate;

    /**
     * @param delegate - projection to transform
     */
    public ProjectionTransform(GeographicProjection delegate) {
        this.delegate = delegate;
    }

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
