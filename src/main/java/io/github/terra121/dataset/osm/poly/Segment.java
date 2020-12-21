package io.github.terra121.dataset.osm.poly;

import io.github.terra121.util.interval.Interval;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A lightweight representation of a line segment with no additional data.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
final class Segment implements Interval {
    public final double lon0;
    public final double lat0;
    public final double lon1;
    public final double lat1;

    @Override
    public double min() {
        return Math.min(this.lon0, this.lon1);
    }

    @Override
    public double max() {
        return Math.max(this.lon0, this.lon1);
    }
}
