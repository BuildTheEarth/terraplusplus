package net.buildtheearth.terraplusplus.crs.cs;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.NonNull;
import net.buildtheearth.terraplusplus.crs.cs.axis.Axis;
import net.buildtheearth.terraplusplus.util.Internable;

/**
 * A coordinate system.
 *
 * @author DaPorkchop_
 */
@Data
public abstract class CoordinateSystem implements Internable<CoordinateSystem> {
    /**
     * A {@link ImmutableList list} of the {@link Axis axes} which make up this coordinate system.
     */
    @NonNull
    private final ImmutableList<Axis> axes;
}
