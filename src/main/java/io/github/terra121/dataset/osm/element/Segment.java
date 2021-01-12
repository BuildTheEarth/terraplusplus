package io.github.terra121.dataset.osm.element;

import io.github.terra121.util.bvh.Bounds2d;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static java.lang.Math.*;

/**
 * A simple representation of a line segment as a pair of two sets of coordinates.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public final class Segment implements Bounds2d {
    protected final double x0;
    protected final double z0;
    protected final double x1;
    protected final double z1;

    @Override
    public double minX() {
        return min(this.x0, this.x1);
    }

    @Override
    public double maxX() {
        return max(this.x0, this.x1);
    }

    @Override
    public double minZ() {
        return min(this.z0, this.z1);
    }

    @Override
    public double maxZ() {
        return max(this.z0, this.z1);
    }
}
