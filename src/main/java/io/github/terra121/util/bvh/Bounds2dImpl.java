package io.github.terra121.util.bvh;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Trivial implementation of {@link Bounds2d}.
 *
 * @author DaPorkchop_
 */
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
class Bounds2dImpl implements Bounds2d {
    protected double minX;
    protected double maxX;
    protected double minZ;
    protected double maxZ;
}
