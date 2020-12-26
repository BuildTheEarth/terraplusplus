package io.github.terra121.dataset.multires;

import io.github.terra121.util.bvh.Bounds2i;
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
public final class WrappedUrl implements Bounds2i, Comparable<WrappedUrl> {
    @NonNull
    protected final String url;

    protected final int minX;
    protected final int maxX;
    protected final int minZ;
    protected final int maxZ;

    protected final int zoom;

    protected final double priority;

    @Override
    public int compareTo(WrappedUrl o) {
        return -Double.compare(this.priority, o.priority);
    }
}
