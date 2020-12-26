package io.github.terra121.util.bvh;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Trivial implementation of {@link Bounds2i}.
 *
 * @author DaPorkchop_
 */
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
class Bounds2iImpl implements Bounds2i {
    protected final int minX;
    protected final int maxX;
    protected final int minZ;
    protected final int maxZ;
}
