package io.github.terra121.dataset.osm.element;

import io.github.terra121.util.bvh.Bounds2d;
import io.github.terra121.util.interval.Interval;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A simple representation of a line segment as a pair of two sets of coordinates.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public final class Segment implements Bounds2d, Interval {
    protected final double x0;
    protected final double z0;
    protected final double x1;
    protected final double z1;

    //bounds2d

    @Override
    public double minX() {
        return Math.min(this.x0, this.x1);
    }

    @Override
    public double maxX() {
        return Math.max(this.x0, this.x1);
    }

    @Override
    public double minZ() {
        return Math.min(this.z0, this.z1);
    }

    @Override
    public double maxZ() {
        return Math.max(this.z0, this.z1);
    }

    //interval

    @Override
    public double min() {
        return this.minX();
    }

    @Override
    public double max() {
        return this.maxX();
    }
}
