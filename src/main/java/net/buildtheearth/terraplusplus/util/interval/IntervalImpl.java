package net.buildtheearth.terraplusplus.util.interval;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Trivial implementation of {@link Interval}.
 *
 * @author DaPorkchop_
 */
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
class IntervalImpl implements Interval {
    protected final double min;
    protected final double max;
}
