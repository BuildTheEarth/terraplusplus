package io.github.terra121.dataset.multires;

import io.github.terra121.util.bvh.Bounds2d;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Wrapper around a dataset URL with a bounding box.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class WrappedUrl implements Bounds2d, Comparable<WrappedUrl> {
    @NonNull
    protected final String url;

    protected final double minX;
    protected final double maxX;
    protected final double minZ;
    protected final double maxZ;

    protected final int zoom;

    protected final double priority;

    @Override
    public int compareTo(WrappedUrl o) {
        return Double.compare(this.priority, o.priority);
    }
}
