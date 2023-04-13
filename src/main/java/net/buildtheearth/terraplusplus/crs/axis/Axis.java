package net.buildtheearth.terraplusplus.crs.axis;

import lombok.Data;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.axis.unit.AxisUnit;
import net.buildtheearth.terraplusplus.util.InternHelper;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * A single axis in a coordinate system.
 *
 * @author DaPorkchop_
 */
@Data
public final class Axis implements Internable<Axis> {
    /**
     * This axis' name.
     */
    @NonNull
    private final String name;

    /**
     * This axis' direction.
     */
    @NonNull
    private final AxisDirection direction;

    /**
     * The {@link AxisUnit unit} used for values on this axis.
     */
    @NonNull
    private final AxisUnit unit;

    /**
     * The minimum coordinate value allowed on this axis, or {@code null} if no such limit exists.
     */
    private final Number minValue;

    /**
     * The maximum coordinate value allowed on this axis, or {@code null} if no such limit exists.
     */
    private final Number maxValue;

    /**
     * If {@code true}, indicates that coordinate values on this axis wrap around at the {@link #minValue() minimum} and {@link #maxValue() maximum} bounds (which
     * must be set).
     * <p>
     * If {@code false}, coordinate values do not wrap around, and the {@link #minValue() minimum} and {@link #maxValue() maximum} bounds (if present) should be
     * treated as an exact limit.
     */
    private final boolean wraparound;

    @Override
    public Axis intern() {
        String name = this.name.intern();
        AxisUnit unit = this.unit.intern();

        //noinspection StringEquality
        return InternHelper.intern(name != this.name || unit != this.unit
                ? new Axis(name, this.direction, unit, this.minValue, this.maxValue, this.wraparound)
                : this);
    }
}
