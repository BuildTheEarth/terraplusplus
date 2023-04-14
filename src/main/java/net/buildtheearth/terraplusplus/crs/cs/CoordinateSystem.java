package net.buildtheearth.terraplusplus.crs.cs;

import com.google.common.collect.ImmutableList;
import net.buildtheearth.terraplusplus.crs.cs.axis.Axis;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * A coordinate system.
 *
 * @author DaPorkchop_
 */
public interface CoordinateSystem extends Internable<CoordinateSystem> {
    /**
     * A {@link ImmutableList list} of the {@link Axis axes} which make up this coordinate system.
     */
    ImmutableList<Axis> axes();

    /**
     * @return this coordinate system's dimension, equivalent to {@code this.axes().size()}
     */
    default int dimension() {
        return this.axes().size();
    }
}
